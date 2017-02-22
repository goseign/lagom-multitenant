package optrak.lagomtest.plan.api

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, AggregateEventTagger}
import optrak.lagomtest.data.Data._
import optrak.lagomtest.data.DataJson._
import play.api.libs.json._

/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object PlanEvents {

  sealed trait PlanEvent extends AggregateEvent[PlanEvent] {
    def planId: PlanId

    override def aggregateTag: AggregateEventTagger[PlanEvent] = PlanEvent.Tag
  }

  case class PlanCreated(planDescription: PlanDescription) extends PlanEvent {
    def planId = planDescription.id
  }

  case object PlanCreated {
    implicit def format: Format[PlanCreated] = Json.format[PlanCreated]
  }

  case class ProductUpdated(planId: PlanId, product: Product) extends PlanEvent

  object ProductUpdated {
    implicit val format: Format[ProductUpdated] = Json.format[ProductUpdated]
  }

  case class ProductRemoved(planId: PlanId, productId: ProductId) extends PlanEvent

  case object ProductRemoved {
    implicit def format: Format[ProductRemoved] = Json.format[ProductRemoved]
  }


  case class SiteUpdated(planId: PlanId, site: Site) extends PlanEvent

  object SiteUpdated {
    implicit val format: Format[SiteUpdated] = Json.format[SiteUpdated]
  }

  case class SiteRemoved(planId: PlanId, siteId: SiteId) extends PlanEvent

  case object SiteRemoved {
    implicit def format: Format[SiteRemoved] = Json.format[SiteRemoved]
  }

  implicit val reads: Reads[PlanEvent] = {
      (__ \ "event_type").read[String].flatMap {
        case "planCreated" => implicitly[Reads[PlanCreated]].map(identity)
        case "productUpdated" => implicitly[Reads[ProductUpdated]].map(identity)
        case "productRemoved" => implicitly[Reads[ProductRemoved]].map(identity)
        case "siteUpdated" => implicitly[Reads[SiteUpdated]].map(identity)
        case "siteRemoved" => implicitly[Reads[SiteRemoved]].map(identity)
      }
    }
    implicit val writes: Writes[PlanEvent] = Writes { event =>
      val (jsValue, eventType) = event match {
        case m: PlanCreated => (Json.toJson(m)(PlanCreated.format), "planCreated")
        case m: ProductUpdated => (Json.toJson(m)(ProductUpdated.format), "productUpdated")
        case m: ProductRemoved => (Json.toJson(m)(ProductRemoved.format), "productRemoved")
        case m: SiteUpdated => (Json.toJson(m)(SiteUpdated.format), "siteUpdated")
        case m: SiteRemoved => (Json.toJson(m)(SiteRemoved.format), "siteRemoved")
      }
      jsValue.transform(__.json.update((__ \ 'event_type).json.put(JsString(eventType)))).get
    }

  case object PlanEvent {
    val NumShards = 4
    val Tag = AggregateEventTag.sharded[PlanEvent](NumShards)
  }
}
