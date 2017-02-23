package optrak.lagomtest.orders.api

import com.lightbend.lagom.scaladsl.api.broker.Topic
import play.api.libs.json._
import optrak.lagomtest.data.Data._

/**
* Created by tim on 22/01/17.
* Copyright Tim Pigden, Hertford UK
  * Note this is different from  oiptrak.lagomtest.orders.impl.OrderEvents in the way the events are defined
*/
object OrderEvents {

  sealed trait OrderEvent {
    def tenantId: TenantId

    def orderId: OrderId
  }

  case class OrderCreated(tenantId: TenantId, orderId: OrderId) extends OrderEvent

}

