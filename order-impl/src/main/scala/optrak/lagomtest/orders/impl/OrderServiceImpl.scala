package optrak.lagomtest.orders.impl

import akka.{Done, NotUsed}
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import grizzled.slf4j.Logging
import optrak.lagomtest.data.Data.{Order, OrderId, TenantId}
import optrak.lagomtest.orders.api.OrderEvents.{OrderCreated => ApiOrderCreated, OrderEvent => ApiOrderEvent}
import optrak.lagomtest.orders.api._
import optrak.lagomtest.orders.impl.OrderEvents.{OrderCreated, OrderEvent}
import optrak.lagomtest.products.api.ProductService
import optrak.lagomtest.sites.api.SiteService

import scala.concurrent.ExecutionContext

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class OrderServiceImpl(persistentEntityRegistry: PersistentEntityRegistry,
                       orderRepository: OrderRepository,
                       siteService: SiteService,
                       productService: ProductService)
                        (implicit ec: ExecutionContext)
  extends OrderService with Logging {

  // note because we're runing a multi-tenancy app, the tenantId must be part of the entity id -
  // may have same order codes
  def entityId(tenantId: TenantId, order: OrderId) = s"$tenantId:$order"

  def ref(tenantId: TenantId, id: OrderId) =
      persistentEntityRegistry.refFor[OrderEntity](entityId(tenantId, id))


  override def createOrder(tenantId: TenantId, id: OrderId): ServiceCall[OrderCreationData, Done] = ServiceCall { request =>
    logger.debug(s"creating order $id")
    for {
      _ <- siteService.checkSiteExists(tenantId, request.site).invoke()
      _ <- productService.checkProductExists(tenantId, request.product).invoke()
      res <- ref(tenantId, id).ask(CreateOrder(tenantId, id, request.site, request.product, request.quantity))
    } yield {
      logger.debug(s"created order $id")
      res
    }
  }
  override def updateQuantity(tenantId: TenantId, id: OrderId, newQuantity: Int): ServiceCall[NotUsed, Done] = ServiceCall { request =>
    ref(tenantId, id).ask(UpdateOrderQuantity(tenantId, id, newQuantity))
  }

  override def getOrder(tenantId: TenantId, id: OrderId): ServiceCall[NotUsed, Order] = ServiceCall { request =>
    ref(tenantId, id).ask(GetOrder).map {
      case Some(order) => order
      case None => throw NotFound(s"Order ${ref(tenantId, id)} not found")
    }
  }

  override def getOrdersForTenant(tenantId: TenantId): ServiceCall[NotUsed, OrderIds] = ServiceCall { _ =>
    orderRepository.selectOrdersForTenant(tenantId)
  }

}


