package optrak.lagomtest.products.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.Service.pathCall
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}
import optrak.lagomtest.datamodel.Models._
import optrak.lagomtest.datamodel.ModelsJson._

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  * the tenant for whom we are managing the products
  * product id - nb externally defined so 2 tenants could have different products with same id.
  *
  * Refer to datamodel for description of data model
  */
trait ProductService extends Service {

  def createProduct(tenant: TenantId, id: ProductId): ServiceCall[ProductCreationData, Done]

  def updateSize(tenant: TenantId, id: ProductId, newSize: Int): ServiceCall[NotUsed, Done]

  def updateGroup(tenant: TenantId, id: ProductId, newGroup: String): ServiceCall[NotUsed, Done]

  def getProduct(tenant: TenantId, id: ProductId): ServiceCall[NotUsed, Product]

  def cancelProduct(tenant: TenantId, id: ProductId): ServiceCall[NotUsed, Done]

  def getProductsForTenant(tenant: TenantId): ServiceCall[NotUsed, Seq[ProductStatus]]

  def getLiveProductsForTenant(tenantId: TenantId): ServiceCall[NotUsed, Seq[ProductId]]


  override final def descriptor = {
    import Service._

    named("product").withCalls(
      pathCall("/optrak.lagom.products.api/:tenant/size/:id/:newSize", updateSize _),
      pathCall("/optrak.lagom.products.api/:tenant/group/:id/:newGroup", updateGroup _),
      pathCall("/optrak.lagom.products.api/:tenant/create/:id", createProduct _),
      pathCall("/optrak.lagom.products.api/:tenant/product/:id", getProduct _ ),
      pathCall("/optrak.lagom.products.api/:tenant/products", getProductsForTenant _ ),
      pathCall("/optrak.lagom.products.api/:tenant/liveProducts", getLiveProductsForTenant _ ),
      pathCall("/optrak.lagom.products.api/:tenant/cancel/:id", cancelProduct _ )
    ).withAutoAcl(true)
  }

}

case class ProductCreationData(size: Int, group: String)

case class ProductStatus(productId: ProductId, isCancelled: Boolean)

object ProductCreationData{
  implicit val format: Format[ProductCreationData] = Json.format[ProductCreationData]
}




