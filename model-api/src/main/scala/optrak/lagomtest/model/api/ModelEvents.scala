package optrak.lagomtest.model.api

import java.awt.event.ItemEvent

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, AggregateEventTagger}
import optrak.lagomtest.datamodel.Models._
import optrak.lagomtest.datamodel.ModelsJson._
import play.api.libs.json._

/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object ModelEvents {

  sealed trait ModelEvent extends AggregateEvent[ModelEvent] {
    def modelId: ModelId

    override def aggregateTag: AggregateEventTagger[ModelEvent] = ModelEvent.Tag
  }

  case class ModelCreated(modelDescription: ModelDescription) extends ModelEvent {
    def modelId = modelDescription.id
  }

  case object ModelCreated {
    implicit def format: Format[ModelCreated] = Json.format[ModelCreated]
  }

  case class ProductUpdated(modelId: ModelId, product: Product) extends ModelEvent

  object ProductUpdated {
    implicit val format: Format[ProductUpdated] = Json.format[ProductUpdated]
  }

  case class ProductRemoved(modelId: ModelId, productId: ProductId) extends ModelEvent

  case object ProductRemoved {
    implicit def format: Format[ProductRemoved] = Json.format[ProductRemoved]
  }


  case class SiteUpdated(modelId: ModelId, site: Site) extends ModelEvent

  object SiteUpdated {
    implicit val format: Format[SiteUpdated] = Json.format[SiteUpdated]
  }

  case class SiteRemoved(modelId: ModelId, siteId: SiteId) extends ModelEvent

  case object SiteRemoved {
    implicit def format: Format[SiteRemoved] = Json.format[SiteRemoved]
  }

  implicit val reads: Reads[ModelEvent] = {
      (__ \ "event_type").read[String].flatMap {
        case "modelCreated" => implicitly[Reads[ModelCreated]].map(identity)
        case "productUpdated" => implicitly[Reads[ProductUpdated]].map(identity)
        case "productRemoved" => implicitly[Reads[ProductRemoved]].map(identity)
        case "siteUpdated" => implicitly[Reads[SiteUpdated]].map(identity)
        case "siteRemoved" => implicitly[Reads[SiteRemoved]].map(identity)
      }
    }
    implicit val writes: Writes[ModelEvent] = Writes { event =>
      val (jsValue, eventType) = event match {
        case m: ModelCreated => (Json.toJson(m)(ModelCreated.format), "modelCreated")
        case m: ProductUpdated => (Json.toJson(m)(ProductUpdated.format), "productUpdated")
        case m: ProductRemoved => (Json.toJson(m)(ProductRemoved.format), "productRemoved")
        case m: SiteUpdated => (Json.toJson(m)(SiteUpdated.format), "siteUpdated")
        case m: SiteRemoved => (Json.toJson(m)(SiteRemoved.format), "siteRemoved")
      }
      jsValue.transform(__.json.update((__ \ 'event_type).json.put(JsString(eventType)))).get
    }

  case object ModelEvent {
    val NumShards = 4
    val Tag = AggregateEventTag.sharded[ModelEvent](NumShards)
  }
}
