package optrak.lagomtest.vehicles.api

import akka.{Done, NotUsed}
import cats.data.Validated.Valid
import com.lightbend.lagom.scaladsl.api.Service.pathCall
import com.lightbend.lagom.scaladsl.api.deser
import com.lightbend.lagom.scaladsl.api.deser.{MessageSerializer, StrictMessageSerializer}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import grizzled.slf4j.Logging
import optrak.lagom.utils
import optrak.lagomtest.data.Data._
import optrak.scalautils.json.JsonImplicits._
import optrak.lagom.utils.PlayJson4s._
import optrak.scalautils.validating.ErrorReports.{EitherER, ValidatedER, ValidationContext}
import optrak.lagom.utils.{CsvSerializer, XlsSerializer, XmlSerializer}
import optrak.lagomtest.vehicles.api.VehicleService.Vehicles
import optrak.scalautils.data.common.Headers.{BuiltInInputHeaders, BuiltInOutputHeaders}
import optrak.scalautils.xml.{XmlParser, XmlWriter}
import optrak.lagom.utils.CheckedDoneSerializer._
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  */

object VehicleService {
  type Vehicles = List[Vehicle]
}
trait VehicleService extends Service with Logging {

  def updateCapacity(tenant: TenantId, id: VehicleId, newCapacity: Int): ServiceCall[NotUsed, Done]

  def getVehicle(tenant: TenantId, id: VehicleId): ServiceCall[NotUsed, Vehicle]

  def getVehiclesForTenant(tenant: TenantId): ServiceCall[NotUsed, VehicleIds]

  // nb we don't need this as implicit because we have to choose xls or csv
  val vehiclesCsvSerializer: StrictMessageSerializer[EitherER[Vehicles]] = {
    import optrak.scalautils.data.csv.CsvImplicits._
    implicit val inputHeaders = BuiltInInputHeaders
    implicit val onputHeaders = BuiltInOutputHeaders
    import utils.CsvSerializer._
      CsvSerializer.csvFormatMessageSerializer[Vehicle]
  }

  // nb we don't need this as implicit because we have to choose xls or csv
  val vehiclesXlsSerializer: StrictMessageSerializer[EitherER[Vehicles]] = {
    import optrak.scalautils.data.poi.PoiImplicits._
    implicit val inputHeaders = BuiltInInputHeaders
    implicit val onputHeaders = BuiltInOutputHeaders
    import utils.XlsSerializer._
    XlsSerializer.xlsFormatMessageSerializer[Vehicle]
  }


  def createVehiclesFromCsv(tenantId: TenantId): ServiceCall[EitherER[Vehicles], CheckedDone]

  implicit val xmlVehicleCreationSerializer: StrictMessageSerializer[VehicleCreationData] = {
    import optrak.scalautils.xml.XmlImplicits._
    val vcdParser = XmlParser[VehicleCreationData]
    val vcdWriter = XmlWriter[VehicleCreationData]

    val xmlS = XmlSerializer.XmlMessageSerializer

    XmlSerializer.xmlFormatMessageSerializer[VehicleCreationData](xmlS, vcdParser, vcdWriter)
  }

  def createVehiclesFromXls(tenantId: TenantId): ServiceCall[EitherER[Vehicles], CheckedDone]

  def createVehicleXml(tenant: TenantId, id: VehicleId): ServiceCall[VehicleCreationData, Done]

  override final def descriptor = {
    import Service._

    named("vehicle").withCalls(
      pathCall("/optrak.lagom.vehicles.api/:tenant/capacity/:id/:newCapacity", updateCapacity _),
      pathCall("/optrak.lagom.vehicles.api/:tenant/create/:id", createVehicleXml _),
      {
        // this is different from usual patter because both xls and csv serializers could fulfil the implicit
        implicit val serializer: MessageSerializer[EitherER[Vehicles], _] = vehiclesCsvSerializer
        pathCall("/optrak.lagom.vehicles.api/:tenant/fromCsv", createVehiclesFromCsv _)
      },
      {
        // this is different from usual patter because both xls and csv serializers could fulfil the implicit
        implicit val serializer: MessageSerializer[EitherER[Vehicles], _] = vehiclesXlsSerializer
        pathCall("/optrak.lagom.vehicles.api/:tenant/fromXls", createVehiclesFromXls _)
      },
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



