package optrak.lagom.utils

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, StringReader, StringWriter}

import akka.util.ByteString
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer.{NegotiatedDeserializer, NegotiatedSerializer}
import com.lightbend.lagom.scaladsl.api.deser.{MessageSerializer, StrictMessageSerializer}
import com.lightbend.lagom.scaladsl.api.transport.{MessageProtocol, SerializationException}
import grizzled.slf4j.Logging
import optrak.scalautils.data.common.HeaderBuilder
import optrak.scalautils.data.common.Headers.{InputHeaders, OutputHeaders}
import optrak.scalautils.data.poi._
import org.apache.poi.ss.usermodel.{Workbook, WorkbookFactory}
import optrak.scalautils.validating.ErrorReports.{EitherER, HeadContext}

import scala.collection.immutable
import scala.util.control.NonFatal

/**
  * Created by timpigden on 24/02/17.
  * Copyright (c) Optrak Distribution Software Ltd, Ware 2016
  *
  * serialize to and from a xls file - which internally is represented as a
  * string until parsed and processed.
  */
object XlsSerializer extends Logging {
  implicit val validationContext = HeadContext("Xls Serializer")

  val xlsProtocol = MessageProtocol(Some("application/vnd.ms-excel"), Some("utf-8"), None)
  val xlsxProtocol = MessageProtocol(Some("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), Some("utf-8"), None)

  abstract class WorkbookMessageSerializer extends StrictMessageSerializer[Workbook] {

    def defaultWorkbookProtocol: MessageProtocol

    private class WorkbookSerializer(override val protocol: MessageProtocol) extends NegotiatedSerializer[Workbook, ByteString] {
      override def serialize(message: Workbook): ByteString = try {

        val outputStream = new ByteArrayOutputStream()
        message.write(outputStream)
        ByteString.fromArray(outputStream.toByteArray)
      } catch {
        case NonFatal(e) => throw SerializationException(e)
      }
    }

    private class WorkbookDeserializer(charset: String) extends NegotiatedDeserializer[Workbook, ByteString] {
      override def deserialize(wire: ByteString): Workbook = {
        val ins = new ByteArrayInputStream(wire.toArray)
        try {
          WorkbookFactory.create(ins)
        } catch {
          case NonFatal(e) => throw SerializationException(e)
        }
      }
    }

    override def deserializer(protocol: MessageProtocol): NegotiatedDeserializer[Workbook, ByteString] =
      new WorkbookDeserializer(defaultWorkbookProtocol.charset.getOrElse("utf-8"))

    override def serializerForResponse(acceptedMessageProtocols: immutable.Seq[MessageProtocol]): NegotiatedSerializer[Workbook, ByteString] = {
      new WorkbookSerializer(acceptedMessageProtocols.find(_.contentType.contains("text/Workbook")).getOrElse(defaultWorkbookProtocol))
    }

    override def serializerForRequest: NegotiatedSerializer[Workbook, ByteString] = new WorkbookSerializer(defaultWorkbookProtocol)
  }

  implicit val XlsMessageSerializer = new WorkbookMessageSerializer {
    override val acceptResponseProtocols: immutable.Seq[MessageProtocol] = immutable.Seq(xlsProtocol)

    override def defaultWorkbookProtocol: MessageProtocol = xlsProtocol
  }

  implicit val XlsxMessageSerializer = new WorkbookMessageSerializer {
    override val acceptResponseProtocols: immutable.Seq[MessageProtocol] = immutable.Seq(xlsxProtocol)

    override def defaultWorkbookProtocol: MessageProtocol = xlsxProtocol
  }


  /**
    * unlike the Csv serializer we need to be explicit about whether we want our output format to be xlsx or xls
    * @param workbookMessageSerializer xls or xlsx?
    */

  implicit def poiFormatMessageSerializer[Message](workbookMessageSerializer: MessageSerializer[Workbook, ByteString])
                                                  (implicit poiParser: PoiCellParser[Message],
                                                   poiWriter: PoiRowWriter[Message],
                                                   headerBuilder: HeaderBuilder[Message],
                                                   inputHeaders: InputHeaders,
                                                   outputHeaders: OutputHeaders)
  : StrictMessageSerializer[EitherER[List[Message]]] = new StrictMessageSerializer[EitherER[List[Message]]] {
    private class PoiFormatSerializer(poiSerializer: NegotiatedSerializer[Workbook, ByteString])
      extends NegotiatedSerializer[EitherER[List[Message]], ByteString] {
      override def protocol: MessageProtocol = poiSerializer.protocol

      override def serialize(message: EitherER[List[Message]]): ByteString = {
        message match {
          case Left(msg) =>  CheckedDoneSerializer.toByteString(Left(msg))
          case Right(data) =>
            val asXls = protocol == xlsProtocol
            val workbook = SpreadsheetWriter.createSpreadsheet(asXls, "data", data, outputHeaders)
            poiSerializer.serialize(workbook)
        }
      }
    }

    private class PoiFormatDeserializer(poiDeserializer: NegotiatedDeserializer[Workbook, ByteString])
      extends NegotiatedDeserializer[EitherER[List[Message]], ByteString] {
      override def deserialize(wire: ByteString): EitherER[List[Message]] = {
        val poi = poiDeserializer.deserialize(wire)
        SpreadsheetTableReader.parseTable(poi, None, inputHeaders)
      }
    }

    override def acceptResponseProtocols: immutable.Seq[MessageProtocol] = workbookMessageSerializer.acceptResponseProtocols

    override def deserializer(protocol: MessageProtocol): NegotiatedDeserializer[EitherER[List[Message]], ByteString] =
      new PoiFormatDeserializer(workbookMessageSerializer.deserializer(protocol))

    override def serializerForResponse(acceptedMessageProtocols: immutable.Seq[MessageProtocol]
                                       = immutable.Seq(xlsProtocol))
    : NegotiatedSerializer[EitherER[List[Message]], ByteString] =
      new PoiFormatSerializer(workbookMessageSerializer.serializerForResponse(acceptedMessageProtocols))

    override def serializerForRequest: NegotiatedSerializer[EitherER[List[Message]], ByteString] =
      new PoiFormatSerializer(workbookMessageSerializer.serializerForRequest)
  }

  def xlsFormatMessageSerializer[Message]
                                                  (implicit poiParser: PoiCellParser[Message],
                                                   poiWriter: PoiRowWriter[Message],
                                                   headerBuilder: HeaderBuilder[Message],
                                                   inputHeaders: InputHeaders,
                                                   outputHeaders: OutputHeaders)
  : StrictMessageSerializer[EitherER[List[Message]]] = poiFormatMessageSerializer[Message](XlsMessageSerializer)

  def xlsxFormatMessageSerializer[Message](implicit poiParser: PoiCellParser[Message],
                                                     poiWriter: PoiRowWriter[Message],
                                                     headerBuilder: HeaderBuilder[Message],
                                                     inputHeaders: InputHeaders,
                                                     outputHeaders: OutputHeaders)
  : StrictMessageSerializer[EitherER[List[Message]]] = poiFormatMessageSerializer[Message](XlsxMessageSerializer)

}
