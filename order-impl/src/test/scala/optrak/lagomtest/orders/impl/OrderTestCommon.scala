package optrak.lagomtest.orders.impl

import optrak.lagomtest.datamodel.Models.{Order, OrderId, TenantId}
import optrak.lagomtest.orders.impl.OrderEvents.OrderCreated


/**
  * Created by tim on 14/02/17.
  * Copyright Tim Pigden, Hertford UK
  */
object OrderTestCommon {

  val tenantId = "tenant1"
  val tenant2 = "tenant2"
  val product1 = "product1"
  val product2 = "product2"
  val site1 = "site1"
  val site2 = "site2"
  val order1Id = "order1"
  val quantity1 = 10
  val order2Id = "order2"
  val quantity2 = 2
  val group1 = "group1"
  val group2 = "group2"
  val order1 = Order(order1Id, site1, product1, quantity1)
  val order2 = Order(order2Id, site2, product1, quantity2)
  val order1sz9 = order1.copy(quantity = 9)

  val createOrder1 = CreateOrder(tenantId, order1Id, site1, product1, quantity1)
  val order1Created = OrderCreated(tenantId, order1Id, site1, product1, quantity1)

  def entityId(tenantId: TenantId, orderId: OrderId) = s"$tenantId:$orderId"

}
