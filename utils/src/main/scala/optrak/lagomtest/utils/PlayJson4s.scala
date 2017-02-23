package optrak.lagomtest.utils

import java.util.UUID

import cats.data.Validated.{Invalid, Valid}
import optrak.scalautils.json.{JsonParser, JsonWriter, SimpleJsonWriters}
import optrak.scalautils.validating.ErrorReports.{HeadContext, ValidatedER, ValidationContext}
import org.json4s.JsonAST.JValue
import play.api.libs.{json => pjson}
import org.json4s.{JsonAST => j4s}
import play.api.data.validation.ValidationError
import play.api.libs.json._
import optrak.scalautils.validating.Exceptions._
import optrak.scalautils.json.JsonImplicits._
import cats.syntax.either._
/**
  * Created by tim on 23/02/17.
  * Copyright Tim Pigden, Hertford UK
  */
object PlayJson4s {

  implicit def uuidParser = new JsonParser[UUID] {
    override def parse(n: JValue)(implicit vContext: ValidationContext): ValidatedER[UUID] =
      (
        for {
        js <-StringParser.parse(n).toEither
        parsed <- exceptionWrapER(UUID.fromString(js))
      } yield parsed
        ).toValidated
  }

  implicit def uuidWriter = new JsonWriter[UUID] {
    override def write(name: Option[String], a: UUID): JValue =
      stringJsonWriter.write(name, a.toString)
  }

  implicit val validationContext = HeadContext("PlayJson4s")


  def toJson4s(json: play.api.libs.json.JsValue): org.json4s.JValue = json match {
    case pjson.JsString(str) => j4s.JString(str)
    case pjson.JsNull => j4s.JNull
    case pjson.JsBoolean(value) => j4s.JBool(value)
    case pjson.JsNumber(value) => j4s.JDecimal(value)
    case pjson.JsArray(items) => j4s.JArray(items.map(toJson4s(_)).toList)
    case pjson.JsObject(items) => j4s.JObject(items.map { case (k, v) => k -> toJson4s(v) }.toList)
  }

  def toPlayJson(json: org.json4s.JValue): play.api.libs.json.JsValue = json match {
    case j4s.JString(str) => pjson.JsString(str)
    case j4s.JNothing => pjson.JsNull
    case j4s.JNull => pjson.JsNull
    case j4s.JDecimal(value) => pjson.JsNumber(value)
    case j4s.JDouble(value) => pjson.JsNumber(value)
    case j4s.JInt(value) => pjson.JsNumber(BigDecimal(value))
    case j4s.JLong(value) => pjson.JsNumber(BigDecimal(value))
    case j4s.JBool(value) => pjson.JsBoolean(value)
    case j4s.JArray(fields) => pjson.JsArray(fields.map(toPlayJson(_)))
    case j4s.JObject(fields) => pjson.JsObject(fields.map { case (k, v) => k -> toPlayJson(v) }.toMap)
  }

  implicit def opkJsonFormat[T](implicit parser: JsonParser[T], writer: JsonWriter[T]): Format[T] = new Format[T] {
    override def writes(o: T): JsValue = {
      toPlayJson(writer.write(None, o))
    }

    override def reads(json: JsValue): JsResult[T] = {
      val asJ4s = toJson4s(json)
      parser.parse(asJ4s) match {
        case Invalid(bad) =>
          val messages = bad.toList
          val error = ValidationError(messages.toString)
          JsError(error)
        case Valid(t) => JsSuccess(t)
      }
    }
  }


}
