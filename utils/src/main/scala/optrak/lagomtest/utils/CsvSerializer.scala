package optrak.lagomtest.utils

import akka.util.ByteString
import cats.data.Validated.{Invalid, Valid}
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer.{NegotiatedDeserializer, NegotiatedSerializer}
import com.lightbend.lagom.scaladsl.api.deser.{MessageSerializer, StrictMessageSerializer}
import com.lightbend.lagom.scaladsl.api.transport.{DeserializationException, MessageProtocol, SerializationException}
import optrak.scalautils.data.common.HeaderBuilder
import optrak.scalautils.data.common.Parsing.CellParser
import optrak.scalautils.data.common.Writing.RowWriter
import optrak.scalautils.data.csv.{CsvRow, CsvRowWriter}
import optrak.scalautils.validating.ErrorReports.HeadContext

import scala.collection.immutable
import scala.util.control.NonFatal

/**
  * Created by timpigden on 24/02/17.
  * Copyright (c) Optrak Distribution Software Ltd, Ware 2016
  *
  * serialize to and from a csv file - which internally is represented as a
  * string until parsed and processed.
  */
object CsvSerializer {
  implicit val validationContext = HeadContext("Csv Serializer")

  val defaultCsvProtocol = MessageProtocol(Some("text/csv"), Some("utf-8"), None)
  
  case class Csv(data: String)

  implicit val CsvMessageSerializer = new StrictMessageSerializer[Csv] {

    override val acceptResponseProtocols: immutable.Seq[MessageProtocol] = immutable.Seq(defaultCsvProtocol)

    private class CsvSerializer(override val protocol: MessageProtocol) extends NegotiatedSerializer[Csv, ByteString] {
      override def serialize(message: Csv): ByteString = try {
        ByteString.fromString(message.data, protocol.charset.getOrElse("utf-8"))
      } catch {
        case NonFatal(e) => throw SerializationException(e)
      }
    }

    private class CsvDeserializer(charset: String) extends NegotiatedDeserializer[Csv, ByteString] {
      override def deserialize(wire: ByteString): Csv = {
        val s = wire.decodeString(charset)
      }
    }

    override def deserializer(protocol: MessageProtocol): NegotiatedDeserializer[Csv, ByteString] =
      new CsvDeserializer(defaultCsvProtocol.charset.getOrElse("utf-8"))

    override def serializerForResponse(acceptedMessageProtocols: immutable.Seq[MessageProtocol]): NegotiatedSerializer[Csv, ByteString] = {
      new CsvSerializer(acceptedMessageProtocols.find(_.contentType.contains("text/csv")).getOrElse(defaultCsvProtocol))
    }

    override def serializerForRequest: NegotiatedSerializer[Csv, ByteString] = new CsvSerializer(defaultCsvProtocol)

  }

  implicit def xmlFormatMessageSerializer[Message](implicit NodeMessageSerializer: MessageSerializer[Csv, ByteString],
                                                   csvParser: CellParser[Message, String, CsvRow],
                                                   csvWriter: CsvRowWriter[Message],
                                                   headerBuilder: HeaderBuilder[Message])
  : StrictMessageSerializer[Message] = new StrictMessageSerializer[Message] {
    private class CsvFormatSerializer(xmlSerializer: NegotiatedSerializer[Csv, ByteString]) extends NegotiatedSerializer[Message, ByteString] {
      override def protocol: MessageProtocol = xmlSerializer.protocol

      override def serialize(message: Message): ByteString = {
        val node = xmlWriter.write(Some("Message"), message)
        xmlSerializer.serialize(node.head)
      }
    }

    private class NodeFormatDeserializer(CsvDeserializer: NegotiatedDeserializer[Csv, ByteString]) extends NegotiatedDeserializer[Message, ByteString] {
      override def deserialize(wire: ByteString): Message = {
        val node = CsvDeserializer.deserialize(wire)
        logger.debug(s"node is $node")
        xmlParser.parse(node) match {
          case Valid(message) => message
          case Invalid(msgs) => throw DeserializationException(msgs.toList.mkString("\n"))
        }
      }
    }

    override def acceptResponseProtocols: immutable.Seq[MessageProtocol] = NodeMessageSerializer.acceptResponseProtocols

    override def deserializer(protocol: MessageProtocol): NegotiatedDeserializer[Message, ByteString] =
      new NodeFormatDeserializer(NodeMessageSerializer.deserializer(protocol))

    override def serializerForResponse(acceptedMessageProtocols: immutable.Seq[MessageProtocol] = immutable.Seq(defaultCsvProtocol)): NegotiatedSerializer[Message, ByteString] =
      new CsvFormatSerializer(NodeMessageSerializer.serializerForResponse(acceptedMessageProtocols))

    override def serializerForRequest: NegotiatedSerializer[Message, ByteString] =
      new CsvFormatSerializer(NodeMessageSerializer.serializerForRequest)
  }

}
