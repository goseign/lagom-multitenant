package com.optrak.products.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}
import com.lightbend.lagom.scaladsl.playjson.{Jsonable, SerializerRegistry, Serializers}

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
trait ProductService extends Service {

  def setProduct(id: String): ServiceCall[ProductUpdate, Done]

  def getProduct(id: String): ServiceCall[NotUsed, Product]


  override final def descriptor = {
    import Service._

    named("product").withCalls(
      pathCall("/com.optrak.products.api/product/:id", setProduct _),
      pathCall("/com.optrak.products.api/product/:id", getProduct _ )
    ).withAutoAcl(true)
  }

}

case class ProductUpdate(size: Int, group: String)

object ProductUpdate{
  implicit val format: Format[ProductUpdate] = Json.format[ProductUpdate]
}

case class Product(id: String, size: Int, group: String)

object Product {
  def apply(id: String, productUpdate: ProductUpdate) = {
    import productUpdate._
    new Product(id, size, group)
  }

  implicit val format: Format[Product] = Json.format[Product]
}


