package optrak.lagomtest.orders.api

import com.lightbend.lagom.scaladsl.api.broker.Topic
import play.api.libs.json._
import optrak.lagomtest.data.Data._
import optrak.lagomtest.data.DataJson._

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

  object OrderCreated {
    implicit def format: Format[OrderCreated] = Json.format[OrderCreated]
  }

  implicit val reads: Reads[OrderEvent] = {
    (__ \ "event_type").read[String].flatMap {
      case "orderCreated" => implicitly[Reads[OrderCreated]].map(identity)
    }
  }
  implicit val writes: Writes[OrderEvent] = Writes { event =>
    val (jsValue, eventType) = event match {
      case m: OrderCreated => (Json.toJson(m)(OrderCreated.format), "orderCreated")
    }
    jsValue.transform(__.json.update((__ \ 'event_type).json.put(JsString(eventType)))).get
  }
}

