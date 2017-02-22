package optrak.lagomtest.orders.impl

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import optrak.lagomtest.data.Data._
import optrak.lagomtest.orders.impl.OrderEvents.OrderCreated
import optrak.lagomtest.products.api.{ProductCreationData, ProductIds, ProductService, ProductStatuses}

import scala.concurrent.Future


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
  val order1 = Order(order1Id, site1, product1, quantity1)
  val order2 = Order(order2Id, site2, product1, quantity2)
  val order1sz9 = order1.copy(quantity = 9)

  val createOrder1 = CreateOrder(tenantId, order1Id, site1, product1, quantity1)
  val order1Created = OrderCreated(tenantId, order1Id, site1, product1, quantity1)

  def entityId(tenantId: TenantId, orderId: OrderId) = s"$tenantId:$orderId"

  case class ProductMock(valid: Set[ProductId]) extends ProductService {

    override def productExists(tenant: TenantId, id: ProductId): ServiceCall[NotUsed, Boolean] = ServiceCall { _ =>
      Future.successful(valid.contains(id))
    }

    override def createProduct(tenant: TenantId, id: ProductId): ServiceCall[ProductCreationData, Done] = ???

    override def updateSize(tenant: TenantId, id: ProductId, newSize: Int): ServiceCall[NotUsed, Done] = ???

    override def updateGroup(tenant: TenantId, id: ProductId, newGroup: String): ServiceCall[NotUsed, Done] = ???

    override def getProduct(tenant: TenantId, id: ProductId): ServiceCall[NotUsed, Product] = ???

    override def cancelProduct(tenant: TenantId, id: ProductId): ServiceCall[NotUsed, Done] = ???

    override def getProductsForTenantEntity(tenant: TenantId): ServiceCall[NotUsed, ProductStatuses] = ???

    override def getLiveProductsForTenantEntity(tenantId: TenantId): ServiceCall[NotUsed, ProductIds] = ???

    override def getProductsForTenantDb(tenant: TenantId): ServiceCall[NotUsed, ProductStatuses] = ???

    override def getLiveProductsForTenantDb(tenantId: TenantId): ServiceCall[NotUsed, ProductIds] = ???
  }

}
