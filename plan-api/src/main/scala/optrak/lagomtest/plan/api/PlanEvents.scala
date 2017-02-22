package optrak.lagomtest.plan.api

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, AggregateEventTagger}
import optrak.lagomtest.data.Data._
import optrak.lagomtest.data.DataJson._
import play.api.libs.json._

/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  * These events are for shipping to the ModelReader 
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

  case class VehicleUpdated(planId: PlanId, Vehicle: Vehicle) extends PlanEvent

  object VehicleUpdated {
    implicit val format: Format[VehicleUpdated] = Json.format[VehicleUpdated]
  }

  case class VehicleRemoved(planId: PlanId, VehicleId: VehicleId) extends PlanEvent

  case object VehicleRemoved {
    implicit def format: Format[VehicleRemoved] = Json.format[VehicleRemoved]
  }

  case class OrderUpdated(planId: PlanId, Order: Order) extends PlanEvent

  object OrderUpdated {
    implicit val format: Format[OrderUpdated] = Json.format[OrderUpdated]
  }

  case class OrderRemoved(planId: PlanId, OrderId: OrderId) extends PlanEvent

  case object OrderRemoved {
    implicit def format: Format[OrderRemoved] = Json.format[OrderRemoved]
  }



  implicit val reads: Reads[PlanEvent] = {
      (__ \ "event_type").read[String].flatMap {
        case "planCreated" => implicitly[Reads[PlanCreated]].map(identity)
        case "productUpdated" => implicitly[Reads[ProductUpdated]].map(identity)
        case "productRemoved" => implicitly[Reads[ProductRemoved]].map(identity)
        case "siteUpdated" => implicitly[Reads[SiteUpdated]].map(identity)
        case "siteRemoved" => implicitly[Reads[SiteRemoved]].map(identity)
        case "vehicleUpdated" => implicitly[Reads[VehicleUpdated]].map(identity)
        case "vehicleRemoved" => implicitly[Reads[VehicleRemoved]].map(identity)
        case "orderUpdated" => implicitly[Reads[OrderUpdated]].map(identity)
        case "orderRemoved" => implicitly[Reads[OrderRemoved]].map(identity)
      }
    }
    implicit val writes: Writes[PlanEvent] = Writes { event =>
      val (jsValue, eventType) = event match {
        case m: PlanCreated => (Json.toJson(m)(PlanCreated.format), "planCreated")
        case m: ProductUpdated => (Json.toJson(m)(ProductUpdated.format), "productUpdated")
        case m: ProductRemoved => (Json.toJson(m)(ProductRemoved.format), "productRemoved")
        case m: SiteUpdated => (Json.toJson(m)(SiteUpdated.format), "siteUpdated")
        case m: SiteRemoved => (Json.toJson(m)(SiteRemoved.format), "siteRemoved")
        case m: OrderUpdated => (Json.toJson(m)(OrderUpdated.format), "orderUpdated")
        case m: OrderRemoved => (Json.toJson(m)(OrderRemoved.format), "orderRemoved")
        case m: VehicleUpdated => (Json.toJson(m)(VehicleUpdated.format), "vehicleUpdated")
        case m: VehicleRemoved => (Json.toJson(m)(VehicleRemoved.format), "vehicleRemoved")
      }
      jsValue.transform(__.json.update((__ \ 'event_type).json.put(JsString(eventType)))).get
    }

  case object PlanEvent {
    val NumShards = 4
    val Tag = AggregateEventTag.sharded[PlanEvent](NumShards)
  }
}
