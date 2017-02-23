package optrak.lagomtest.utils

import akka.util.ByteString
import cats.data.Validated.{Invalid, Valid}
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer.{NegotiatedDeserializer, NegotiatedSerializer}
import com.lightbend.lagom.scaladsl.api.deser.{MessageSerializer, StrictMessageSerializer}
import com.lightbend.lagom.scaladsl.api.transport.{DeserializationException, _}
import optrak.scalautils.validating.ErrorReports.HeadContext
import optrak.scalautils.xml.{XmlParser, XmlWriter}

import scala.collection.immutable
import scala.collection.immutable.Seq
import scala.util.control.NonFatal
import scala.xml.{Node, XML}

/**
  * Created by tim on 23/02/17.
  * Copyright Tim Pigden, Hertford UK
  */
object XmlSerializer {

  implicit val validationContext = HeadContext("Xml Serializer")

  val defaultXmlProtocol = MessageProtocol(Some("application/xml"), None, None)

  implicit val XmlMessageSerializer = new StrictMessageSerializer[Node] {

    override val acceptResponseProtocols: immutable.Seq[MessageProtocol] = immutable.Seq(defaultXmlProtocol)

    private class NodeSerializer(override val protocol: MessageProtocol) extends NegotiatedSerializer[Node, ByteString] {
      override def serialize(message: Node): ByteString = try {
        ByteString.fromString(message.toString, protocol.charset.getOrElse("utf-8"))
      } catch {
        case NonFatal(e) => throw SerializationException(e)
      }
    }

    private object NodeDeserializer extends NegotiatedDeserializer[Node, ByteString] {
      override def deserialize(wire: ByteString): Node = try {
        XML.load(wire.iterator.asInputStream)
      } catch {
        case NonFatal(e) => throw DeserializationException(e)
      }
    }

    override def deserializer(protocol: MessageProtocol): NegotiatedDeserializer[Node, ByteString] = NodeDeserializer

    override def serializerForResponse(acceptedMessageProtocols: immutable.Seq[MessageProtocol]): NegotiatedSerializer[Node, ByteString] = {
      new NodeSerializer(acceptedMessageProtocols.find(_.contentType.contains("application/xml")).getOrElse(defaultXmlProtocol))
    }

    override def serializerForRequest: NegotiatedSerializer[Node, ByteString] = new NodeSerializer(defaultXmlProtocol)

  }

  implicit def xmlFormatMessageSerializer[Message](implicit NodeMessageSerializer: MessageSerializer[Node, ByteString], xmlParser: XmlParser[Message], xmlWriter: XmlWriter[Message])
  : StrictMessageSerializer[Message] = new StrictMessageSerializer[Message] {
    private class XmlFormatSerializer(xmlSerializer: NegotiatedSerializer[Node, ByteString]) extends NegotiatedSerializer[Message, ByteString] {
      override def protocol: MessageProtocol = xmlSerializer.protocol

      override def serialize(message: Message): ByteString = {
        val node = xmlWriter.write(None, message)
        xmlSerializer.serialize(node.head)
      }
    }

    private class NodeFormatDeserializer(NodeDeserializer: NegotiatedDeserializer[Node, ByteString]) extends NegotiatedDeserializer[Message, ByteString] {
      override def deserialize(wire: ByteString): Message = {
        val node = NodeDeserializer.deserialize(wire)
        xmlParser.parse(node) match {
          case Valid(message) => message
          case Invalid(msgs) => throw DeserializationException(msgs.toList.mkString("\n"))
        }
      }
    }

    override def acceptResponseProtocols: immutable.Seq[MessageProtocol] = NodeMessageSerializer.acceptResponseProtocols

    override def deserializer(protocol: MessageProtocol): NegotiatedDeserializer[Message, ByteString] =
      new NodeFormatDeserializer(NodeMessageSerializer.deserializer(protocol))

    override def serializerForResponse(acceptedMessageProtocols: immutable.Seq[MessageProtocol] = immutable.Seq(defaultXmlProtocol)): NegotiatedSerializer[Message, ByteString] =
      new XmlFormatSerializer(NodeMessageSerializer.serializerForResponse(acceptedMessageProtocols))

    override def serializerForRequest: NegotiatedSerializer[Message, ByteString] =
      new XmlFormatSerializer(NodeMessageSerializer.serializerForRequest)
  }

}
