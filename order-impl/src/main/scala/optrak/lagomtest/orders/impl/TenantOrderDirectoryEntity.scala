package optrak.lagomtest.orders.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import grizzled.slf4j.Logging
import optrak.lagomtest.datamodel.Models.{Order, OrderId}
import optrak.lagomtest.orders.impl.OrderEvents.OrderEvent
import optrak.lagomtest.orders.api.OrderEvents.{OrderCancelled => ApiOrderCancelled, OrderCreated => ApiOrderCreated, OrderEvent => ApiOrderEvent}
import optrak.lagomtest.orders.api.OrderIds
import optrak.lagomtest.orders.impl.TenantOrderDirectoryEntity.Innards
import optrak.lagomtest.utils.JsonFormats
import play.api.libs.json._

import scala.concurrent.Future

/**
  * Created by tim on 18/02/17.
  * Copyright Tim Pigden, Hertford UK
  */

object TenantOrderDirectoryEntity {
  case class Innards(all: Set[OrderId])

  object Innards {
    implicit def format: Format[Innards] = Json.format[Innards]
  }
}
class TenantOrderDirectoryEntity extends PersistentEntity with Logging {
  

  override type Command = OrderDirectoryCommand
  override type Event = OrderDirectoryEvent
  override type State = Innards

  override def initialState: Innards = Innards(Set.empty)

  override def behavior: Behavior = {
    case innards: Innards =>
      Actions().onCommand[WrappedCreateOrder, Done] {
        case (WrappedCreateOrder(orderId), ctx, _) =>
          if (innards.all.contains(orderId))
            ctx.done
          else ctx.thenPersist(OrderAddedToDirectory(orderId)) { _ =>
            logger.debug(s"order added to directory $orderId")
            ctx.reply(Done)
          }
      }.onReadOnlyCommand[GetAllOrders.type, OrderIds] {
        case (GetAllOrders, ctx, state) =>
          val res = state.all
          ctx.reply(OrderIds(res))
      }.onEvent {
        case (OrderAddedToDirectory(orderId), innards) =>
          val newAll = innards.all + orderId
          innards.copy(all = newAll)
      }

  }
}

sealed trait OrderDirectoryCommand

case class WrappedCreateOrder(orderId: OrderId) extends OrderDirectoryCommand with ReplyType[Done]

case object GetAllOrders extends OrderDirectoryCommand with ReplyType[OrderIds] {
  implicit def format: Format[GetAllOrders.type] = JsonFormats.singletonFormat(GetAllOrders)
}

object WrappedCreateOrder {
  implicit def format: Format[WrappedCreateOrder] = Json.format[WrappedCreateOrder]
}

sealed trait OrderDirectoryEvent
case class OrderAddedToDirectory(orderId: OrderId) extends OrderDirectoryEvent

object OrderAddedToDirectory {
  implicit def format: Format[OrderAddedToDirectory] = Json.format[OrderAddedToDirectory]
}



