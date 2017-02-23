package optrak.lagomtest.vehicles.impl

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import optrak.lagomtest.data.Data.{TenantId, VehicleId}
import play.api.libs.json.{Format, Json}
/**
* Created by tim on 22/01/17.
* Copyright Tim Pigden, Hertford UK
*/
object VehicleEvents {

  // nb the vehicleevent needs to be aggregateEvent because it is used by read processor and needs an aggregate tag
  sealed trait VehicleEvent extends AggregateEvent[VehicleEvent] {
    override def aggregateTag = VehicleEvent.Tag

    def tenantId: TenantId
    def vehicleId: VehicleId
  }

  object VehicleEvent {
    val NumShards = 4
    val Tag = AggregateEventTag.sharded[VehicleEvent](NumShards)
  }

  case class VehicleCreated(tenantId: TenantId, vehicleId: VehicleId, capacity: Int) extends VehicleEvent
  case class VehicleCapacityUpdated(tenantId: TenantId, vehicleId: VehicleId, newCapacity: Int) extends VehicleEvent


}
