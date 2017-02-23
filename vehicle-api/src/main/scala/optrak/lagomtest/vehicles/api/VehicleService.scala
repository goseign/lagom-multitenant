package optrak.lagomtest.vehicles.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.Service.pathCall
import com.lightbend.lagom.scaladsl.api.deser
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import optrak.lagomtest.data.Data._
import optrak.scalautils.json.JsonImplicits._
import optrak.lagomtest.utils.PlayJson4s._
import optrak.scalautils.validating.ErrorReports.{ValidatedER, ValidationContext}

import scala.xml.NodeSeq
import optrak.lagomtest.utils.XmlSerializer
import optrak.scalautils.xml.{XmlParser, XmlWriter}

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  */
trait VehicleService extends Service {

  def createVehicle(tenant: TenantId, id: VehicleId): ServiceCall[VehicleCreationData, Done]

  def updateCapacity(tenant: TenantId, id: VehicleId, newCapacity: Int): ServiceCall[NotUsed, Done]

  def getVehicle(tenant: TenantId, id: VehicleId): ServiceCall[NotUsed, Vehicle]

  def getVehiclesForTenant(tenant: TenantId): ServiceCall[NotUsed, VehicleIds]

  def createVehicleXml(tenant: TenantId, id: VehicleId): ServiceCall[VehicleCreationData, Done]

  def xmlCall = {
    import optrak.scalautils.xml.XmlImplicits._
    val vcdParser = XmlParser[VehicleCreationData]
    val vcdWriter = XmlWriter[VehicleCreationData]

    val xmlS = XmlSerializer.XmlMessageSerializer
    val xmlSerializer = XmlSerializer.xmlFormatMessageSerializer[VehicleCreationData](xmlS, vcdParser, vcdWriter)

    pathCall("/optrak.lagom.vehicles.api/:tenant/create/:id", createVehicleXml _)(xmlSerializer, deser.MessageSerializer.DoneMessageSerializer)

  }

  override final def descriptor = {
    import Service._

    named("vehicle").withCalls(
      pathCall("/optrak.lagom.vehicles.api/:tenant/capacity/:id/:newCapacity", updateCapacity _),
      pathCall("/optrak.lagom.vehicles.api/:tenant/create/:id", createVehicle _),
      xmlCall,
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


