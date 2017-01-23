package optrak.lagom.products.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{Jsonable, SerializerRegistry, Serializers}
import optrak.lagom.products.api.{Product, ProductUpdate}
import play.api.libs.json.{Format, Json}
import JsonFormats._
import optrak.lagom.products.impl.ProductEvents.{ProductChanged, ProductEvent}
import optrak.lagom.products.impl.ProductEvents.ProductEvent

import scala.collection.immutable.Seq

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ProductEntity extends PersistentEntity {


  override type Command = ProductCommand
  override type Event = ProductEvent
  override type State = ProductState

  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState = EmptyProduct

  /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case EmptyProduct => noProductYet
    case WithProduct(product) => hasProduct
  }

  def noProductYet: Actions = {
    Actions()
    .onCommand[SetProduct, Done] {
      case (SetProduct(productUpdate), ctx, _) =>
        ctx.thenPersist(ProductChanged(Product(entityId, productUpdate)), evt =>
          ctx.reply(Done)
        )
    }.onReadOnlyCommand[GetProduct.type, ProductState] {
      case (GetProduct, ctx, state) =>
        ctx.reply(EmptyProduct)

    }
    .onEvent{
      case (ProductChanged(product), _) =>
        WithProduct(product)
    }
  }

  def hasProduct: Actions =
  {
    Actions().onCommand[SetProduct, Done] {

      // Command handler for the ChangeProduct command
      case (SetProduct(productUpdate), ctx, state) =>
        val newProduct = Product(entityId, productUpdate)
        if (WithProduct(newProduct) != state)
          ctx.thenPersist(ProductChanged(newProduct),
            // Then once the event is successfully persisted, we respond with done.
            _ => ctx.reply(Done)
          )
        else ctx.done

//    }.onReadOnlyCommand[GetProduct.type, ProductDesc] {
    }.onReadOnlyCommand[GetProduct.type, ProductState] {
      case (GetProduct, ctx, state) =>
        ctx.reply(state)

    }.onEvent {

      // Event handler for the ProductChanged event
      case (ProductChanged(newProduct), state) =>
        // We simply update the current state to use the greeting message from
        // the event.
        WithProduct(newProduct)

    }
  }
}

// --------------- commands -------------------------
sealed trait ProductCommand extends Jsonable

case object GetProduct extends ProductCommand with ReplyType[ProductState] {
  implicit val format: Format[GetProduct.type] = singletonFormat(GetProduct)
}

case class SetProduct(productUpdate: ProductUpdate) extends ProductCommand with ReplyType[Done]

object SetProduct {
  implicit val format: Format[SetProduct] = Json.format[SetProduct]
}

// --------------- state -------------------------
sealed trait ProductState extends Jsonable

case class WithProduct(product: Product) extends ProductState

object WithProduct {
  implicit val format: Format[WithProduct] = Json.format[WithProduct]
}

case object EmptyProduct extends ProductState {
  implicit val format: Format[EmptyProduct.type] = singletonFormat(EmptyProduct)
}






