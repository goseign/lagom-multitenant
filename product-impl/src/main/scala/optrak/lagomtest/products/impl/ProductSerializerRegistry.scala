package optrak.lagomtest.products.impl

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

import scala.collection.immutable.Seq

/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object ProductSerializerRegistry extends JsonSerializerRegistry {

  override def serializers: Seq[JsonSerializer[_]] = {
    val res = Seq(
      JsonSerializer[CreateProduct],
      JsonSerializer[CreateModel],
      JsonSerializer[ProductCreated],
      JsonSerializer[ModelCreated],
      JsonSerializer[ApiCreateProduct],
      JsonSerializer[ApiCreateModel],
      JsonSerializer[ApiModelCreated],
      JsonSerializer[ApiRemoveModel],
      JsonSerializer[RemoveModel],
      JsonSerializer[ModelRemoved]

    )
    res
  }
}