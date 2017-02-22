package optrak.lagomtest.products.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.Service.pathCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}
import optrak.lagomtest.data.Data._
import optrak.lagomtest.data.DataJson._
import optrak.lagomtest.products.api.ProductEvents.ProductEvent

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  * the tenant for whom we are managing the products
  * product id - nb externally defined so 2 tenants could have different products with same id.
  *
  * Refer to datamodel for description of data model
  *
  * Note that for testing purposes only we have 2 different implementations of the product directory.
  *
  * One uses a readside processor that stores the data in Cassandra as tables and issues queries to return the information.
  * That database stores all product info in a single table across separate tenants, using the cql to get data for specific
  * tenant
  *
  * The other uses a readside processor in combination with a directory per tenant as an entity. I've no clear idea about
  * which is "better" or why for this use case but expect that factors will be how long it takes to reconstruct the entity,
  * what the update frequency is, how often requests are made and so on.
  */
trait ProductService extends Service {

  def createProduct(tenant: TenantId, id: ProductId): ServiceCall[ProductCreationData, Done]

  def updateSize(tenant: TenantId, id: ProductId, newSize: Int): ServiceCall[NotUsed, Done]

  def updateGroup(tenant: TenantId, id: ProductId, newGroup: String): ServiceCall[NotUsed, Done]

  def getProduct(tenant: TenantId, id: ProductId): ServiceCall[NotUsed, Product]

  def productExists(tenant: TenantId, id: ProductId): ServiceCall[NotUsed, Boolean]

  def cancelProduct(tenant: TenantId, id: ProductId): ServiceCall[NotUsed, Done]

  def getProductsForTenantEntity(tenant: TenantId): ServiceCall[NotUsed, ProductStatuses]

  def getLiveProductsForTenantEntity(tenantId: TenantId): ServiceCall[NotUsed, ProductIds]

  def getProductsForTenantDb(tenant: TenantId): ServiceCall[NotUsed, ProductStatuses]

  def getLiveProductsForTenantDb(tenantId: TenantId): ServiceCall[NotUsed, ProductIds]

  override final def descriptor = {
    import Service._

    named("product").withCalls(
      pathCall("/optrak.lagom.products.api/:tenant/size/:id/:newSize", updateSize _),
      pathCall("/optrak.lagom.products.api/:tenant/group/:id/:newGroup", updateGroup _),
      pathCall("/optrak.lagom.products.api/:tenant/create/:id", createProduct _),
      pathCall("/optrak.lagom.products.api/:tenant/product/:id", getProduct _ ),
      pathCall("/optrak.lagom.products.api/:tenant/productsDb", getProductsForTenantDb _ ),
      pathCall("/optrak.lagom.products.api/:tenant/liveProductsDb", getLiveProductsForTenantDb _ ),
      pathCall("/optrak.lagom.products.api/:tenant/productsEntity", getProductsForTenantEntity _ ),
      pathCall("/optrak.lagom.products.api/:tenant/liveProductsEntity", getLiveProductsForTenantEntity _ ),
      pathCall("/optrak.lagom.products.api/:tenant/cancel/:id", cancelProduct _ )
    )
      /*
    .withTopics(
      topic("product-directoryEvent", this.productEvents)
      .addProperty(KafkaProperties.partitionKeyStrategy,
        PartitionKeyStrategy[ProductEvent](_.tenantId))
    ) */
    .withAutoAcl(true)
  }

  // def productEvents: Topic[ProductEvent]


}

case class ProductCreationData(size: Int, group: String)

case class ProductStatus(productId: ProductId, isCancelled: Boolean)

case class ProductStatuses(statuses: Set[ProductStatus])

case class ProductIds(ids: Set[ProductId])

object ProductCreationData{
  implicit val format: Format[ProductCreationData] = Json.format[ProductCreationData]
}

object ProductStatus {
  implicit val format: Format[ProductStatus] = Json.format[ProductStatus]
}

object ProductStatuses {
  implicit val format: Format[ProductStatuses] = Json.format[ProductStatuses]
}

object ProductIds {
  implicit val format: Format[ProductIds] = Json.format[ProductIds]
}



