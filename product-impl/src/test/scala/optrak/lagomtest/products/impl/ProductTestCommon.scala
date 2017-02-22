package optrak.lagomtest.products.impl

import optrak.lagomtest.data.Data.{Product, ProductId, TenantId}


/**
  * Created by tim on 14/02/17.
  * Copyright Tim Pigden, Hertford UK
  */
object ProductTestCommon {

  val tenantId = "tenant1"
  val tenant2 = "tenant2"
  val product1Id = "product1"
  val product1Size = 10
  val product2Id = "product2"
  val product2Size = 2
  val group1 = "group1"
  val group2 = "group2"
  val product1 = Product(product1Id, product1Size, group1, false)
  val product2 = Product(product2Id, product2Size, group2, false)
  val product1sz9 = product1.copy(size = 9)

  val product1g2 = product1.copy(group = group2)

  val product1Cancelled = product1.copy(cancelled = true)

  def entityId(tenantId: TenantId, productId: ProductId) = s"$tenantId:$productId"

}
