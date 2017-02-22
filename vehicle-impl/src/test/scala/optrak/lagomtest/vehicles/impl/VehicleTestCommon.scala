package optrak.lagomtest.vehicles.impl

import optrak.lagomtest.data.Data.{TenantId, Vehicle, VehicleId}


/**
  * Created by tim on 14/02/17.
  * Copyright Tim Pigden, Hertford UK
  */
object VehicleTestCommon {

  val tenantId = "tenant1"
  val tenant2 = "tenant2"
  val vehicle1Id = "vehicle1"
  val vehicle1Size = 10
  val vehicle2Id = "vehicle2"
  val vehicle2Size = 2
  val capacity1 = 20
  val capacity2 = 30
  val vehicle1 = Vehicle(vehicle1Id, capacity1)
  val vehicle2 = Vehicle(vehicle2Id, capacity2)

  val vehicle1g2 = vehicle1.copy(capacity = capacity2)

  def entityId(tenantId: TenantId, vehicleId: VehicleId) = s"$tenantId:$vehicleId"

  val createVehicle1 = CreateVehicle(tenantId, vehicle1Id, capacity1)

}
