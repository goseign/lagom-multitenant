package optrak.lagomtest.products.impl

import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.{TransportErrorCode, TransportException}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import optrak.lagomtest.datamodel.Models._
import optrak.lagomtest.products.impl.ProductEvents._
import play.api.libs.json.{Format, Json}
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */

class ProductEntity extends PersistentEntity {

  override type Command = ProductCommand
  override type Event = ProductEvent
  override type State = Option[Product]


  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState = None

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
      case (CreateProduct(clientId, id, size, group), ctx, _) =>
        ctx.thenPersist(ProductCreated(clientId, id, size, group))(evt =>
          ctx.reply(Done)
        )
    }
      .onEvent {
        case (ProductCreated(clientId, id, size, group), _) =>
          Some(Product(id, size, group, false))
      }
  }

  def hasProduct: Actions = {
    Actions()
      .onCommand[ProductCommand, Done] {
      case (UpdateProductSize(clientId, id, newSize), ctx, _) =>
        ctx.thenPersist(ProductSizeUpdated(clientId, id, newSize))(_ =>
          ctx.reply(Done))
      case (UpdateProductGroup(clientId, id, newGroup), ctx, _) =>
        ctx.thenPersist(ProductGroupUpdated(clientId, id, newGroup))(_ =>
          ctx.reply(Done))
      case (CancelProduct(clientId, id), ctx, _) =>
        ctx.thenPersist(ProductCancelled(clientId, id))(_ =>
          ctx.reply(Done))
    }.onEvent {
      // Event handler for the ProductChanged event
      case (ProductSizeUpdated(clientId, id, newSize), Some(product)) =>
        Some(product.copy(size = newSize))
      case (ProductGroupUpdated(clientId, id, newGroup), Some(product)) =>
        Some(product.copy(group = newGroup))
      case (ProductCancelled(clientId, id), Some(product)) =>
        Some(product.copy(cancelled = true))
    }
  }
}

// --------------------------------- internal commands
sealed trait ProductCommand extends ReplyType[Done]
case class CreateProduct(clientId: ClientId, id: String, size: Int, group: String) extends ProductCommand 
case class UpdateProductSize(clientId: ClientId, id: String, newSize: Int) extends ProductCommand 
case class UpdateProductGroup(clientId: ClientId, id: String, newGroup: String) extends ProductCommand 
case class CancelProduct(clientId: ClientId, id: String) extends ProductCommand 

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





