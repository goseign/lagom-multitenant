package optrak.lagom.products.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.Service.pathCall
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}
import optrak.lagomtest.datamodel.Models._
import optrak.lagomtest.datamodel.ModelsJson._

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
trait ProductService extends Service {

  def createProduct(client: String, id: String): ServiceCall[CreateProductData, Done]

  def updateSize(client: String, id: String, newSize: Int): ServiceCall[NotUsed, Done]

  def updateGroup(client: String, id: String, newGroup: String): ServiceCall[NotUsed, Done]

  def getProduct(client: String, id: String): ServiceCall[NotUsed, Product]

  def cancelProduct(client: String, id: String): ServiceCall[NotUsed, Done]


  override final def descriptor = {
    import Service._

    named("product").withCalls(
      pathCall("/optrak.lagom.products.api/:client/size/:id/:newSize", updateSize _),
      pathCall("/optrak.lagom.products.api/:client/group/:id/:newGroup", updateGroup _),
      pathCall("/optrak.lagom.products.api/:client/create/:id", createProduct _),
      pathCall("/optrak.lagom.products.api/:client/product/:id", getProduct _ ),
      pathCall("/optrak.lagom.products.api/:client/cancel/:id", cancelProduct _ )
    ).withAutoAcl(true)
  }

}

case class CreateProductData(size: Int, group: String)

object CreateProductData{
  implicit val format: Format[CreateProductData] = Json.format[CreateProductData]
}


object Product {
  def apply(id: String, updateProduct: CreateProductData) = {
    import updateProduct._
    new Product(id, size, group, false)
  }

  implicit val format: Format[Product] = Json.format[Product]
}


