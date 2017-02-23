package optrak.lagomtest.products.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import grizzled.slf4j.Logging
import optrak.lagomtest.data.Data.{Product, ProductId}
import optrak.lagomtest.products.impl.ProductEvents.ProductEvent
import optrak.lagomtest.products.api.ProductEvents.{ProductCancelled => ApiProductCancelled, ProductCreated => ApiProductCreated, ProductEvent => ApiProductEvent}
import optrak.lagomtest.products.api.{ProductIds, ProductStatus, ProductStatuses}
import optrak.lagomtest.products.impl.TenantProductDirectoryEntity.Innards
import play.api.libs.json._

/**
  * Created by tim on 18/02/17.
  * Copyright Tim Pigden, Hertford UK
  */

object TenantProductDirectoryEntity {
  case class Innards(live: Set[ProductId], cancelled: Set[ProductId]) {
    lazy val all = live ++ cancelled
  }
}
class TenantProductDirectoryEntity extends PersistentEntity with Logging {
  

  override type Command = ProductDirectoryCommand 
  override type Event = ProductDirectoryEvent
  override type State = Innards

  override def initialState: Innards = Innards(Set.empty, Set.empty)

  override def behavior: Behavior = {
    case innards: Innards =>
      Actions().onCommand[WrappedCreateProduct, Done] {
        case (WrappedCreateProduct(productId), ctx, _) =>
          if (innards.all.contains(productId))
            ctx.done
          else ctx.thenPersist(ProductAddedToDirectory(productId)) { _ =>
            logger.debug(s"product added to directory $productId")
            ctx.reply(Done)
          }
      }.onCommand[WrappedCancelProduct, Done] {
        case (WrappedCancelProduct(productId), ctx, _) =>
          if (innards.cancelled.contains(productId) || !innards.live.contains(productId))
            ctx.done
          else ctx.thenPersist(ProductCancelledInDirectory(productId)) { _ =>
            logger.debug(s"product added to directory $productId")
            ctx.reply(Done)
          }
      }.onReadOnlyCommand[GetAllProducts.type, ProductStatuses] {
        case (GetAllProducts, ctx, state) =>
          val res = state.live.map(pid => ProductStatus(pid, false)) ++ state.cancelled.map(pid => ProductStatus(pid, true))
          ctx.reply(ProductStatuses(res))
      }.onReadOnlyCommand[GetLiveProducts.type, ProductIds] {
        case (GetLiveProducts, ctx, state) => ctx.reply(ProductIds(state.live))
      }.onEvent {
        case (ProductCancelledInDirectory(productId), innards) =>
          val newLive = innards.live - productId
          val newCancelled = innards.cancelled + productId
          Innards(newLive, newCancelled)
        case (ProductAddedToDirectory(productId), innards) =>
          val newLive = innards.live + productId
          innards.copy(live = newLive)
      }

  }
}

sealed trait ProductDirectoryCommand

case class WrappedCreateProduct(productId: ProductId) extends ProductDirectoryCommand with ReplyType[Done]

case class WrappedCancelProduct(productId: ProductId) extends ProductDirectoryCommand with ReplyType[Done]

case object GetAllProducts extends ProductDirectoryCommand with ReplyType[ProductStatuses]
case object GetLiveProducts extends ProductDirectoryCommand with ReplyType[ProductIds]
sealed trait ProductDirectoryEvent 
case class ProductAddedToDirectory(productId: ProductId) extends ProductDirectoryEvent
case class ProductCancelledInDirectory(productId: ProductId) extends ProductDirectoryEvent



