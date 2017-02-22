package optrak.lagomtest.orders.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import optrak.lagomtest.data.Data._
import play.api.libs.json.{Format, Json}
/**
* Created by tim on 22/01/17.
* Copyright Tim Pigden, Hertford UK
*/
object OrderEvents {

  // nb the orderevent needs to be aggregateEvent because it is used by read processor and needs an aggregate tag
  sealed trait OrderEvent extends AggregateEvent[OrderEvent] {
    override def aggregateTag = OrderEvent.Tag

    def tenantId: TenantId
    def orderId: OrderId
  }

  object OrderEvent {
    val NumShards = 4
    val Tag = AggregateEventTag.sharded[OrderEvent](NumShards)
  }

  case class OrderCreated(tenantId: TenantId, 
                          orderId: OrderId, 
                          siteId: SiteId, 
                          productId: ProductId,
                          quantity: Int) extends OrderEvent
  case class OrderQuantityUpdated(tenantId: TenantId, orderId: OrderId, newQuantity: Int) extends OrderEvent

  object OrderCreated {
    implicit def format: Format[OrderCreated] = Json.format[OrderCreated]
  }

  object OrderQuantityUpdated {
    implicit def format: Format[OrderQuantityUpdated] = Json.format[OrderQuantityUpdated]
  }

}
