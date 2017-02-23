package optrak.lagomtest.plan.api

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, AggregateEventTagger}
import optrak.lagomtest.data.Data._

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

  case class ProductUpdated(planId: PlanId, product: Product) extends PlanEvent

  case class ProductRemoved(planId: PlanId, productId: ProductId) extends PlanEvent

  case class SiteUpdated(planId: PlanId, site: Site) extends PlanEvent

  case class SiteRemoved(planId: PlanId, siteId: SiteId) extends PlanEvent

  case class VehicleUpdated(planId: PlanId, Vehicle: Vehicle) extends PlanEvent

  case class VehicleRemoved(planId: PlanId, VehicleId: VehicleId) extends PlanEvent

  case class OrderUpdated(planId: PlanId, Order: Order) extends PlanEvent

  case class OrderRemoved(planId: PlanId, OrderId: OrderId) extends PlanEvent


  case object PlanEvent {
    val NumShards = 4
    val Tag = AggregateEventTag.sharded[PlanEvent](NumShards)
  }
}
