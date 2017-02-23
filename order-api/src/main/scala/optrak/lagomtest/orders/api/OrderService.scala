package optrak.lagomtest.orders.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import optrak.lagomtest.data.Data._
import optrak.scalautils.json.JsonImplicits._
import optrak.lagomtest.utils.PlayJson4s._

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  * the tenant for whom we are managing the orders
  * order id - nb externally defined so 2 tenants could have different orders with same id.
  *
  * Refer to datamodel for description of data model
  */
trait OrderService extends Service {

  def createOrder(tenant: TenantId, id: OrderId): ServiceCall[OrderCreationData, Done]

  def updateQuantity(tenant: TenantId, id: OrderId, newQuantity: Int): ServiceCall[NotUsed, Done]

  def getOrder(tenant: TenantId, id: OrderId): ServiceCall[NotUsed, Order]

  def getOrdersForTenant(tenant: TenantId): ServiceCall[NotUsed, OrderIds]


  override final def descriptor = {
    import Service._

    named("order").withCalls(
      pathCall("/optrak.lagom.orders.api/:tenant/quantity/:id/:newQuantity", updateQuantity _),
      pathCall("/optrak.lagom.orders.api/:tenant/create/:id", createOrder _),
      pathCall("/optrak.lagom.orders.api/:tenant/order/:id", getOrder _ ),
      pathCall("/optrak.lagom.orders.api/:tenant/orders", getOrdersForTenant _ )
    ).withAutoAcl(true)
  }

  // def orderEvents: Topic[OrderEven


}

case class OrderCreationData(site: SiteId, product: ProductId, quantity: Int)

case class OrderIds(ids: Set[OrderId])



