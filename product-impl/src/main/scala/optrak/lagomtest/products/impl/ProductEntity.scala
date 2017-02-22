package optrak.lagomtest.products.impl

import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.{TransportErrorCode, TransportException}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import grizzled.slf4j.Logging
import optrak.lagomtest.data.Data._
import optrak.lagomtest.products.impl.ProductEvents._
import optrak.lagomtest.utils.JsonFormats
import play.api.libs.json.{Format, Json}
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */


case class ProductAlreadyExistsException(tenantId: TenantId, productId: ProductId)
  extends TransportException(TransportErrorCode.UnsupportedData, s"product $productId for tenant $tenantId already exists")

class ProductEntity extends PersistentEntity with Logging {

  override type Command = ProductCommand
  override type Event = ProductEvent
  override type State = Option[Product]


  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState = None
  
  private val getProductCommand = Actions().onReadOnlyCommand[GetProduct.type, Option[Product]] {
    case (GetProduct, ctx, state) => ctx.reply(state)
  }
      /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case None => noProductYet
    case Some(s) => hasProduct
  }

  def noProductYet: Actions = {
    Actions()
      .onCommand[CreateProduct, Done] {
      case (CreateProduct(tenantId, id, size, group), ctx, _) =>
        ctx.thenPersist(ProductCreated(tenantId, id, size, group)) { evt =>
          logger.debug(s"creating product $tenantId $id")
          ctx.reply(Done)
        }
    }.onEvent {
        case (ProductCreated(tenantId, id, size, group), _) =>
          val update = Some(Product(id, size, group, false))
          logger.debug(s"updated model for $id")
          update
      }.orElse(getProductCommand)

  }

  def hasProduct: Actions = {
    Actions()
      .onCommand[CreateProduct, Done] {
      case (CreateProduct(tenantId, id, size, group), ctx, _) =>
        throw new ProductAlreadyExistsException(tenantId, id)
    }.onCommand[UpdateProductSize, Done] {
      case (UpdateProductSize(tenantId, id, newSize), ctx, _) =>
        ctx.thenPersist(ProductSizeUpdated(tenantId, id, newSize))(_ =>
          ctx.reply(Done))
    }.onCommand[UpdateProductGroup, Done] {
      case (UpdateProductGroup(tenantId, id, newGroup), ctx, _) =>
        ctx.thenPersist(ProductGroupUpdated(tenantId, id, newGroup))(_ =>
          ctx.reply(Done))
    }.onCommand[CancelProduct, Done] {
      case (CancelProduct(tenantId, id), ctx, _) =>
        ctx.thenPersist(ProductCancelled(tenantId, id)){_ =>
          logger.debug(s"cancelling product $tenantId $id")
          ctx.reply(Done)}
    }.onEvent {
      // Event handler for the ProductChanged event
      case (ProductSizeUpdated(tenantId, id, newSize), Some(product)) =>
        Some(product.copy(size = newSize))
      case (ProductGroupUpdated(tenantId, id, newGroup), Some(product)) =>
        Some(product.copy(group = newGroup))
      case (ProductCancelled(tenantId, id), Some(product)) =>
        Some(product.copy(cancelled = true))
    }.orElse(getProductCommand)
  }
}

// --------------------------------- internal commands

sealed trait ProductCommand

sealed trait ProductDoCommand extends ProductCommand with ReplyType[Done]
case class CreateProduct(tenantId: TenantId, id: String, size: Int, group: String) extends ProductDoCommand
case class UpdateProductSize(tenantId: TenantId, id: String, newSize: Int) extends ProductDoCommand
case class UpdateProductGroup(tenantId: TenantId, id: String, newGroup: String) extends ProductDoCommand
case class CancelProduct(tenantId: TenantId, id: String) extends ProductDoCommand

case object GetProduct extends ProductCommand with ReplyType[Option[Product]] {
  implicit def format: Format[GetProduct.type] = JsonFormats.singletonFormat(GetProduct)
}

object CreateProduct {
  implicit def format: Format[CreateProduct] = Json.format[CreateProduct]
}

object UpdateProductSize {
  implicit def format: Format[UpdateProductSize] = Json.format[UpdateProductSize]
}
object UpdateProductGroup {
  implicit def format: Format[UpdateProductGroup] = Json.format[UpdateProductGroup]
}
object CancelProduct {
  implicit def format: Format[CancelProduct] = Json.format[CancelProduct]
}





