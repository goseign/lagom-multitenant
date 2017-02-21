package optrak.lagomtest.orders.impl

import akka.{Done, NotUsed}
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import grizzled.slf4j.Logging
import optrak.lagomtest.datamodel.Models.{Order, OrderId, TenantId}
import optrak.lagomtest.orders.api.OrderEvents.{OrderCreated => ApiOrderCreated, OrderEvent => ApiOrderEvent}
import optrak.lagomtest.orders.api._
import optrak.lagomtest.orders.impl.OrderEvents.{OrderCreated, OrderEvent}

import scala.concurrent.ExecutionContext

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class OrderServiceImpl(persistentEntityRegistry: PersistentEntityRegistry
                         // ,orderRepository: OrderRepository
                        )
                        (implicit ec: ExecutionContext)
  extends OrderService with Logging {

  // note because we're runing a multi-tenancy app, the tenantId must be part of the entity id -
  // may have same order codes
  def entityId(tenantId: TenantId, order: OrderId) = s"$tenantId:$order"

  def ref(tenantId: TenantId, id: OrderId) =
      persistentEntityRegistry.refFor[OrderEntity](entityId(tenantId, id))

  def directoryRef(tenantId: TenantId) =
    persistentEntityRegistry.refFor[TenantOrderDirectoryEntity](tenantId)


  override def createOrder(tenantId: TenantId, id: OrderId): ServiceCall[OrderCreationData, Done] = ServiceCall { request =>
    logger.debug(s"creating order $id")
    ref(tenantId, id).ask(CreateOrder(tenantId, id, request.site, request.product, request.quantity)).map { res =>
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
    directoryRef(tenantId).ask(GetAllOrders)
  }

  override def orderEvents: Topic[ApiOrderEvent] = TopicProducer.taggedStreamWithOffset(OrderEvent.Tag.allTags.toList) { (tag, offset) =>
    persistentEntityRegistry.eventStream(tag, offset).map { t =>
      val event = t.event
      ref(event.tenantId, event.orderId).ask(GetOrder).flatMap {
        case Some(order) =>
          event match {
            case pc: OrderCreated =>
              logger.debug(s"impl got orderCreated $pc")
              directoryRef(event.tenantId).ask(WrappedCreateOrder(event.orderId)).map { _ =>
                ApiOrderCreated(event.tenantId, order.id)
              }
          }
      }.map(x => (x, offset))
    }.mapAsync(1)(identity)

  }
}

