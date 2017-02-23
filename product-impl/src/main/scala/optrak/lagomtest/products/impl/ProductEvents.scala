package optrak.lagomtest.products.impl

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import optrak.lagomtest.data.Data.{PlanId, ProductId, TenantId}
/**
* Created by tim on 22/01/17.
* Copyright Tim Pigden, Hertford UK
*/
object ProductEvents {

  // nb the productevent needs to be aggregateEvent because it is used by read processor and needs an aggregate tag
  sealed trait ProductEvent extends AggregateEvent[ProductEvent] {
    override def aggregateTag = ProductEvent.Tag

    def tenantId: TenantId
    def productId: ProductId
  }

  object ProductEvent {
    val NumShards = 4
    val Tag = AggregateEventTag.sharded[ProductEvent](NumShards)
  }

  case class ProductCreated(tenantId: TenantId, productId: ProductId, size: Int, group: String) extends ProductEvent
  case class ProductSizeUpdated(tenantId: TenantId, productId: ProductId, newSize: Int) extends ProductEvent
  case class ProductGroupUpdated(tenantId: TenantId, productId: ProductId, newGroup: String) extends ProductEvent
  case class ProductCancelled(tenantId: TenantId, productId: ProductId) extends ProductEvent


}
