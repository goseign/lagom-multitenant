package optrak.lagomtest.vehicles.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import optrak.lagomtest.data.Data._
import optrak.scalautils.json.JsonImplicits._
import optrak.lagomtest.utils.PlayJson4s._

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  * the tenant for whom we are managing the vehicles
  * vehicle id - nb externally defined so 2 tenants could have different vehicles with same id.
  *
  * Refer to datamodel for description of data model
  *
  * Note that for testing purposes only we have 2 different implementations of the vehicle directory.
  *
  * One uses a readside processor that stores the data in Cassandra as tables and issues queries to return the information.
  * That database stores all vehicle info in a single table across separate tenants, using the cql to get data for specific
  * tenant
  *
  * The other uses a readside processor in combination with a directory per tenant as an entity. I've no clear idea about
  * which is "better" or why for this use case but expect that factors will be how long it takes to reconstruct the entity,
  * what the update frequency is, how often requests are made and so on.
  */
trait VehicleService extends Service {

  def createVehicle(tenant: TenantId, id: VehicleId): ServiceCall[VehicleCreationData, Done]

  def updateCapacity(tenant: TenantId, id: VehicleId, newCapacity: Int): ServiceCall[NotUsed, Done]

  def getVehicle(tenant: TenantId, id: VehicleId): ServiceCall[NotUsed, Vehicle]

  def getVehiclesForTenant(tenant: TenantId): ServiceCall[NotUsed, VehicleIds]


  override final def descriptor = {
    import Service._

    named("vehicle").withCalls(
      pathCall("/optrak.lagom.vehicles.api/:tenant/capacity/:id/:newCapacity", updateCapacity _),
      pathCall("/optrak.lagom.vehicles.api/:tenant/create/:id", createVehicle _),
      pathCall("/optrak.lagom.vehicles.api/:tenant/vehicle/:id", getVehicle _ ),
      pathCall("/optrak.lagom.vehicles.api/:tenant/vehicles", getVehiclesForTenant _ )
    )
      /*
    .withTopics(
      topic("vehicle-directoryEvent", this.vehicleEvents)
      .addProperty(KafkaProperties.partitionKeyStrategy,
        PartitionKeyStrategy[VehicleEvent](_.tenantId))
    ) */
    .withAutoAcl(true)
  }

  // def vehicleEvents: Topic[VehicleEvent]


}

case class VehicleCreationData(capacity: Int)

case class VehicleIds(ids: Set[VehicleId])


