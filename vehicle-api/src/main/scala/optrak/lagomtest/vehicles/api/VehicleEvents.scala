package optrak.lagomtest.vehicles.api

import optrak.lagomtest.data.Data._
import play.api.libs.json._

/**
* Created by tim on 22/01/17.
* Copyright Tim Pigden, Hertford UK
  * Note this is different from  oiptrak.lagomtest.vehicles.impl.VehicleEvents in the way the events are defined
*/
object VehicleEvents {

  sealed trait VehicleEvent {
    def tenantId: TenantId

    def vehicleId: VehicleId
  }

  case class VehicleCreated(tenantId: TenantId, vehicleId: VehicleId) extends VehicleEvent

}

