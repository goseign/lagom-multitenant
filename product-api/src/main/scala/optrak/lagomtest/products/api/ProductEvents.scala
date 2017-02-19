package optrak.lagomtest.products.api

import com.lightbend.lagom.scaladsl.api.broker.Topic
import play.api.libs.json._
import optrak.lagomtest.datamodel.Models._
import optrak.lagomtest.datamodel.ModelsJson._

/**
* Created by tim on 22/01/17.
* Copyright Tim Pigden, Hertford UK
  * Note this is different from  oiptrak.lagomtest.products.impl.ProductEvents in the way the events are defined
*/
object ProductEvents {

  sealed trait ProductEvent {
    def tenantId: TenantId

    def productId: ProductId
  }

  case class ProductCreated(tenantId: TenantId, productId: ProductId) extends ProductEvent

  // unlike the product entity itself, we don't care how it's been updated, we just want the new version
  case class ProductCancelled(tenantId: TenantId, productId: ProductId) extends ProductEvent

  object ProductCreated {
    implicit def format: Format[ProductCreated] = Json.format[ProductCreated]
  }

  object ProductCancelled {
    implicit def format: Format[ProductCancelled] = Json.format[ProductCancelled]
  }

  implicit val reads: Reads[ProductEvent] = {
    (__ \ "event_type").read[String].flatMap {
      case "productCreated" => implicitly[Reads[ProductCreated]].map(identity)
      case "productCancelled" => implicitly[Reads[ProductCancelled]].map(identity)
    }
  }
  implicit val writes: Writes[ProductEvent] = Writes { event =>
    val (jsValue, eventType) = event match {
      case m: ProductCreated => (Json.toJson(m)(ProductCreated.format), "productCreated")
      case m: ProductCancelled => (Json.toJson(m)(ProductCancelled.format), "productCancelled")
    }
    jsValue.transform(__.json.update((__ \ 'event_type).json.put(JsString(eventType)))).get
  }
}

