package optrak.lagomtest.orders.impl

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import optrak.lagomtest.data.Data._
import optrak.lagomtest.orders.impl.OrderEvents.OrderCreated
import optrak.lagomtest.products.api.{ProductCreationData, ProductIds, ProductService, ProductStatuses}
import optrak.lagomtest.sites.api.{SiteCreationData, SiteIds, SiteService}

import scala.concurrent.Future


/**
  * Created by tim on 14/02/17.
  * Copyright Tim Pigden, Hertford UK
  */
object OrderTestCommon {

  val tenantId = "tenant1"
  val tenant2 = "tenant2"
  val product1Id = "product1"
  val product2Id = "product2"
  val site1Id = "site1"
  val site2Id = "site2"
  val order1Id = "order1"
  val quantity1 = 10
  val order2Id = "order2"
  val quantity2 = 2
  val order1 = Order(order1Id, site1Id, product1Id, quantity1)
  val order2 = Order(order2Id, site2Id, product1Id, quantity2)
  val order1sz9 = order1.copy(quantity = 9)

  val createOrder1 = CreateOrder(tenantId, order1Id, site1Id, product1Id, quantity1)
  val order1Created = OrderCreated(tenantId, order1Id, site1Id, product1Id, quantity1)

  def entityId(tenantId: TenantId, orderId: OrderId) = s"$tenantId:$orderId"

  case class ProductMock(valid: Set[ProductId]) extends ProductService {

    override def checkProductExists(tenant: TenantId, id: ProductId): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
      if (valid.contains(id))
        Future.successful(Done)
      else throw throw NotFound(s"Product $tenantId:$id not found")
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

  case class SiteMock(valid: Set[SiteId]) extends SiteService {
    override def createSite(tenant: TenantId, id: SiteId): ServiceCall[SiteCreationData, Done] = ???

    override def updatePostcode(tenant: TenantId, id: SiteId, newPostcode: String): ServiceCall[NotUsed, Done] = ???

    override def getSite(tenant: TenantId, id: SiteId): ServiceCall[NotUsed, Site] = ???

    override def getSitesForTenant(tenant: TenantId): ServiceCall[NotUsed, SiteIds] = ???

    override def checkSiteExists(tenant: TenantId, id: SiteId): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
      if (valid.contains(id))
        Future.successful(Done)
      else throw throw NotFound(s"Site $tenantId:$id not found")
    }

  }
}
