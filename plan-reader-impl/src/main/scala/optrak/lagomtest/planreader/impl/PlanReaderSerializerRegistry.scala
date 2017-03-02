package optrak.lagomtest.planreader.impl

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

import scala.collection.immutable.Seq
import optrak.scalautils.json.JsonImplicits._
import optrak.lagom.utils.PlayJson4s._
/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object PlanReaderSerializerRegistry extends JsonSerializerRegistry {

  override def serializers: Seq[JsonSerializer[_]] = {
    val res = Seq(
      JsonSerializer[WrappedPlanEvent]
    )
    res
  }
}