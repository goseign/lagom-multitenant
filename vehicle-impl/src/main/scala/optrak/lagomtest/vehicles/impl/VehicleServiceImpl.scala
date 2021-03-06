package optrak.lagomtest.vehicles.impl

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import grizzled.slf4j.Logging
import optrak.lagomtest.data.Data.{TenantId, Vehicle, VehicleId}
import optrak.lagomtest.vehicles.api.VehicleEvents.{VehicleCreated => ApiVehicleCreated, VehicleEvent => ApiVehicleEvent}
import optrak.lagomtest.vehicles.api._

import scala.concurrent.ExecutionContext

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class VehicleServiceImpl(persistentEntityRegistry: PersistentEntityRegistry,
                          vehicleRepository: VehicleRepository
                        )
                        (implicit ec: ExecutionContext)
  extends VehicleService with Logging {

  // note because we're runing a multi-tenancy app, the tenantId must be part of the entity id -
  // may have same vehicle codes
  def entityId(tenantId: TenantId, vehicle: VehicleId) = s"$tenantId:$vehicle"

  def ref(tenantId: TenantId, id: VehicleId) =
      persistentEntityRegistry.refFor[VehicleEntity](entityId(tenantId, id))


  override def createVehicle(tenantId: TenantId, id: VehicleId): ServiceCall[VehicleCreationData, Done] = ServiceCall { request =>
    logger.debug(s"creating vehicle $id")
    ref(tenantId, id).ask(CreateVehicle(tenantId, id, request.capacity)).map { res =>
      logger.debug(s"created vehicle $id")
      res
    }
  }
  override def updateCapacity(tenantId: TenantId, id: VehicleId, newCapacity: Int): ServiceCall[NotUsed, Done] = ServiceCall { request =>
    ref(tenantId, id).ask(UpdateVehicleCapacity(tenantId, id, newCapacity))
  }

  override def getVehicle(tenantId: TenantId, id: VehicleId): ServiceCall[NotUsed, Vehicle] = ServiceCall { request =>
    ref(tenantId, id).ask(GetVehicle).map {
      case Some(vehicle) => vehicle
      case None => throw NotFound(s"Vehicle ${ref(tenantId, id)} not found")
    }
  }

  override def getVehiclesForTenant(tenantId: TenantId): ServiceCall[NotUsed, VehicleIds] = ServiceCall { _ =>
    vehicleRepository.selectVehiclesForTenant(tenantId)
  }

}


