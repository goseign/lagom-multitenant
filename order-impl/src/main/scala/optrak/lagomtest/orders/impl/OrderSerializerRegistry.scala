package optrak.lagomtest.orders.impl

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import optrak.lagomtest.data.Data.{Order, OrderId}
import optrak.lagomtest.orders.impl.OrderEvents._
import optrak.scalautils.json.JsonImplicits._
import optrak.lagom.utils .PlayJson4s._
import optrak.lagomtest.orders.api.OrderIds

import scala.collection.immutable.Seq

/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object OrderSerializerRegistry extends JsonSerializerRegistry {

  override def serializers: Seq[JsonSerializer[_]] = {
    val res = Seq(
      JsonSerializer[CreateOrder],
      JsonSerializer[UpdateOrderQuantity],
      JsonSerializer[GetOrder.type],
      JsonSerializer[Order],
      JsonSerializer[OrderCreated],
      JsonSerializer[OrderQuantityUpdated],
      JsonSerializer[OrderIds]

    )
    res
  }
}