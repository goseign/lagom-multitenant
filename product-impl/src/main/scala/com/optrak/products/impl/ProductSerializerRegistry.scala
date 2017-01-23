package com.optrak.products.impl

import com.lightbend.lagom.scaladsl.playjson.{SerializerRegistry, Serializers}
import com.optrak.products.api.Product
import com.optrak.products.api.Product._

import scala.collection.immutable.Seq
import JsonFormats._
import com.optrak.products.impl.ProductEvents.ProductChanged
import play.api.libs.json.{Format, Json}

/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ProductSerializerRegistry extends SerializerRegistry {

  implicitly[Format[Product]]

  override def serializers: Seq[Serializers[_]] = {
    val res = Seq(
      Serializers[SetProduct],
      Serializers[Product],
      Serializers[WithProduct],
      Serializers[ProductChanged],
      Serializers[EmptyProduct.type],
      Serializers[GetProduct.type]
    )
    res
  }
}