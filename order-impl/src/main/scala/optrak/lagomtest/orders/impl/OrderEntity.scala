package optrak.lagomtest.orders.impl

import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.{TransportErrorCode, TransportException}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import grizzled.slf4j.Logging
import optrak.lagomtest.data.Data._
import optrak.lagomtest.orders.impl.OrderEvents._
import optrak.scalautils.json.JsonImplicits._

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */


case class OrderAlreadyExistsException(tenantId: TenantId, orderId: OrderId)
  extends TransportException(TransportErrorCode.UnsupportedData, s"order $orderId for tenant $tenantId already exists")

class OrderEntity extends PersistentEntity with Logging {

  override type Command = OrderCommand
  override type Event = OrderEvent
  override type State = Option[Order]


  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState = None
  
  private val getOrderCommand = Actions().onReadOnlyCommand[GetOrder.type, Option[Order]] {
    case (GetOrder, ctx, state) => ctx.reply(state)
  }
      /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case None => noOrderYet
    case Some(s) => hasOrder
  }

  def noOrderYet: Actions = {
    Actions()
      .onCommand[CreateOrder, Done] {
      case (CreateOrder(tenantId, id, site, product, quantity: Int), ctx, _) =>
        ctx.thenPersist(OrderCreated(tenantId, id, site, product, quantity)) { evt =>
          logger.debug(s"creating order $tenantId $id")
          ctx.reply(Done)
        }
    }.onEvent {
        case (OrderCreated(tenantId, id, site, product, quantity), _) =>
          val update = Some(Order(id, site, product, quantity))
          logger.debug(s"updated model for $id")
          update
      }.orElse(getOrderCommand)

  }

  def hasOrder: Actions = {
    Actions()
      .onCommand[CreateOrder, Done] {
      case (CreateOrder(tenantId, id, site, product, quantity), ctx, _) =>
        throw new OrderAlreadyExistsException(tenantId, id)
    }.onCommand[UpdateOrderQuantity, Done] {
      case (UpdateOrderQuantity(tenantId, id, newQuantity), ctx, _) =>
        ctx.thenPersist(OrderQuantityUpdated(tenantId, id, newQuantity))(_ =>
          ctx.reply(Done))
    }.onEvent {
      // Event handler for the OrderChanged event
      case (OrderQuantityUpdated(tenantId, id, newQuantity), Some(order)) =>
        Some(order.copy(quantity = newQuantity))
    }.orElse(getOrderCommand)
  }
}

// --------------------------------- internal commands

sealed trait OrderCommand

sealed trait OrderDoCommand extends OrderCommand with ReplyType[Done]
case class CreateOrder(tenantId: TenantId, id: String, siteId: SiteId, productId: ProductId, quantity: Int) extends OrderDoCommand
case class UpdateOrderQuantity(tenantId: TenantId, id: String, newQuantity: Int) extends OrderDoCommand

case object GetOrder extends OrderCommand with ReplyType[Option[Order]]




