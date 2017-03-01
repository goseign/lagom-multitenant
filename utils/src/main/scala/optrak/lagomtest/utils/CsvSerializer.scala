package optrak.lagomtest.utils

import java.io.{StringReader, StringWriter}

import akka.util.ByteString
import cats.data.Validated.{Invalid, Valid}
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer.{NegotiatedDeserializer, NegotiatedSerializer}
import com.lightbend.lagom.scaladsl.api.deser.{MessageSerializer, StrictMessageSerializer}
import com.lightbend.lagom.scaladsl.api.transport.{DeserializationException, MessageProtocol, SerializationException}
import grizzled.slf4j.Logging
import optrak.scalautils.data.common.HeaderBuilder
import optrak.scalautils.data.common.Headers.{InputHeaders, OutputHeaders}
import optrak.scalautils.data.common.Parsing.CellParser
import optrak.scalautils.data.common.Writing.RowWriter
import optrak.scalautils.data.csv.CsvWriting.CsvDataWriter
import optrak.scalautils.data.csv.{CsvCellParser, CsvRow, CsvRowWriter, CsvTableReader}
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
object CsvSerializer extends Logging {
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
        ???
      }
    }

    override def deserializer(protocol: MessageProtocol): NegotiatedDeserializer[Csv, ByteString] =
      new CsvDeserializer(defaultCsvProtocol.charset.getOrElse("utf-8"))

    override def serializerForResponse(acceptedMessageProtocols: immutable.Seq[MessageProtocol]): NegotiatedSerializer[Csv, ByteString] = {
      new CsvSerializer(acceptedMessageProtocols.find(_.contentType.contains("text/csv")).getOrElse(defaultCsvProtocol))
    }

    override def serializerForRequest: NegotiatedSerializer[Csv, ByteString] = new CsvSerializer(defaultCsvProtocol)

  }

  implicit def csvFormatMessageSerializer[Message](implicit csvMessageSerializer: MessageSerializer[Csv, ByteString],
                                                   csvParser: CsvCellParser[Message],
                                                   csvWriter: CsvRowWriter[Message],
                                                   headerBuilder: HeaderBuilder[Message],
                                                   inputHeaders: InputHeaders,
                                                   outputHeaders: OutputHeaders)
  : StrictMessageSerializer[List[Message]] = new StrictMessageSerializer[List[Message]] {
    private class CsvFormatSerializer(csvSerializer: NegotiatedSerializer[Csv, ByteString])
      extends NegotiatedSerializer[List[Message], ByteString] {
      override def protocol: MessageProtocol = csvSerializer.protocol

      override def serialize(message: List[Message]): ByteString = {
        val stringWriter = new StringWriter
        CsvDataWriter.write(message, stringWriter, outputHeaders)
        val csv = Csv(stringWriter.toString)
        csvSerializer.serialize(csv)
      }
    }

    private class CsvFormatDeserializer(CsvDeserializer: NegotiatedDeserializer[Csv, ByteString])
      extends NegotiatedDeserializer[List[Message], ByteString] {
      override def deserialize(wire: ByteString): List[Message] = {
        val csv = CsvDeserializer.deserialize(wire)
        val reader = new StringReader(csv.data)
        val didRead = CsvTableReader.parseTable(reader, inputHeaders)
        didRead match {
          case Right(message) => message
          case Left(msgs) => throw DeserializationException(msgs.toList.mkString("\n"))
        }
      }
    }

    override def acceptResponseProtocols: immutable.Seq[MessageProtocol] = csvMessageSerializer.acceptResponseProtocols

    override def deserializer(protocol: MessageProtocol): NegotiatedDeserializer[List[Message], ByteString] =
      new CsvFormatDeserializer(csvMessageSerializer.deserializer(protocol))

    override def serializerForResponse(acceptedMessageProtocols: immutable.Seq[MessageProtocol] = immutable.Seq(defaultCsvProtocol))
    : NegotiatedSerializer[List[Message], ByteString] =
      new CsvFormatSerializer(csvMessageSerializer.serializerForResponse(acceptedMessageProtocols))

    override def serializerForRequest: NegotiatedSerializer[List[Message], ByteString] =
      new CsvFormatSerializer(csvMessageSerializer.serializerForRequest)
  }

}
