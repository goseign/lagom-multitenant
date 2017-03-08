package optrak.lagom.utils

import akka.Done
import akka.util.ByteString
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer.{NegotiatedDeserializer, NegotiatedSerializer}
import com.lightbend.lagom.scaladsl.api.deser.{LowPriorityMessageSerializerImplicits, MessageSerializer, StrictMessageSerializer}
import com.lightbend.lagom.scaladsl.api.transport.MessageProtocol
import optrak.scalautils.json.JsonErrorReport
import optrak.scalautils.validating.ErrorReports.{EitherER, HeadContext}
import MessageSerializer._
import optrak.scalautils.validating.Force

import scala.collection.immutable

/**
  * Created by timpigden on 24/02/17.
  * Copyright (c) Optrak Distribution Software Ltd, Ware 2016
  *
  * Response to post operation that requires checking of format or other return
  * that is processed directly as a call-return (as opposed to via a reactive mechanism)
  *
  * Will return an EitherER[Done]
  */
object CheckedDoneSerializer extends LowPriorityMessageSerializerImplicits {

  implicit val validationContext = HeadContext("CheckedDone Serializers")
  type CheckedDone = EitherER[Done]

  val acceptJS = JsValueMessageSerializer.acceptResponseProtocols

  implicit val CheckedDoneMessageSerializer: StrictMessageSerializer[CheckedDone] = new StrictMessageSerializer[CheckedDone] {
    override def serializerForRequest = new NegotiatedSerializer[CheckedDone, ByteString] {
      override def serialize(message: CheckedDone): ByteString =
        message match {
          case Left(msgs) =>
            val js4 = JsonErrorReport.errorReportsWriter.write(None, msgs)
            val asPlay = PlayJson4s.toPlayJson(js4)
            JsValueMessageSerializer.serializerForRequest.serialize(asPlay)
          case Right(Done) =>
            DoneMessageSerializer.serializerForRequest.serialize(Done)
        }
    }

    override def deserializer(messageProtocol: MessageProtocol) = new NegotiatedDeserializer[CheckedDone, ByteString] {
      override def deserialize(wire: ByteString) = {
        if (wire == ByteString.empty)
          Right(Done)
        else {
          val asJs = JsValueMessageSerializer.deserializer(acceptJS.head).deserialize(wire)
          val asJ4  = PlayJson4s.toJson4s(asJs)
          val errs = JsonErrorReport.errorReportsParser.parse(asJ4)
          Left(Force.validated(errs))
        }
      }
    }

    override def serializerForResponse(acceptedMessageProtocols: immutable.Seq[MessageProtocol]) = new NegotiatedSerializer[CheckedDone, ByteString] {
      override def serialize(message: CheckedDone): ByteString = {
        message match {
          case Left(msgs) =>
            val js4 = JsonErrorReport.errorReportsWriter.write(None, msgs)
            val asPlay = PlayJson4s.toPlayJson(js4)
            JsValueMessageSerializer.serializerForResponse(acceptJS).serialize(asPlay)
          case Right(Done) =>
            DoneMessageSerializer.serializerForResponse(acceptJS).serialize(Done)
        }
      }
    }
  }

  /**
    * util function called by other serializers if they hit problems
    */
  def toByteString(checkedDone: CheckedDone): ByteString = {
    CheckedDoneMessageSerializer.serializerForResponse(acceptJS).serialize(checkedDone)
  }

}
