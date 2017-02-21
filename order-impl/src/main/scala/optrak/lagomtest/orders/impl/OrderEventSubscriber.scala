package optrak.lagomtest.orders.impl

import akka.Done
import akka.stream.scaladsl.Flow
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import optrak.lagomtest.datamodel.Models.TenantId
import optrak.lagomtest.orders.api.OrderEvents.{OrderEvent => ApiOrderEvent}
import optrak.lagomtest.orders.api.OrderService
import optrak.lagomtest.orders.api.OrderEvents.{OrderCreated => ApiOrderCreated, OrderEvent => ApiOrderEvent}

import scala.concurrent.Future

/**
  * Created by tim on 18/02/17.
  * Copyright Tim Pigden, Hertford UK
  */
class OrderEventSubscriber(persistentEntityRegistry: PersistentEntityRegistry, orderService: OrderService) {

  def ref(tenantId: TenantId) =
    persistentEntityRegistry.refFor[TenantOrderDirectoryEntity](tenantId)

  orderService.orderEvents.subscribe.atLeastOnce(Flow[ApiOrderEvent].mapAsync(1) {

    case evt: ApiOrderCreated =>
      ref(evt.tenantId).ask(WrappedCreateOrder(evt.orderId))

    case other =>
      Future.successful(Done)

  })

}
