package optrak.lagomtest.products.impl

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import optrak.lagomtest.datamodel.Models.Product
import optrak.lagomtest.products.impl.ProductEvents._
import optrak.lagomtest.datamodel.ModelsJson._

import scala.collection.immutable.Seq

/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object ProductSerializerRegistry extends JsonSerializerRegistry {

  override def serializers: Seq[JsonSerializer[_]] = {
    val res = Seq(
      JsonSerializer[CreateProduct],
      JsonSerializer[UpdateProductSize],
      JsonSerializer[UpdateProductGroup],
      JsonSerializer[CancelProduct],
      JsonSerializer[GetProduct.type],
      JsonSerializer[Product],
      JsonSerializer[ProductCreated],
      JsonSerializer[ProductSizeUpdated],
      JsonSerializer[ProductGroupUpdated],
      JsonSerializer[ProductCancelled]

    )
    res
  }
}