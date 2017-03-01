package optrak.lagomtest.vehicles.api

import akka.util.ByteString
import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.Service.pathCall
import com.lightbend.lagom.scaladsl.api.deser
import com.lightbend.lagom.scaladsl.api.deser.{MessageSerializer, StrictMessageSerializer}
import com.lightbend.lagom.scaladsl.api.transport.MessageProtocol
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import optrak.lagomtest.data.Data._
import optrak.lagomtest.utils.CheckedDoneSerializer.CheckedDone
import optrak.scalautils.json.JsonImplicits._
import optrak.lagomtest.utils.PlayJson4s._
import optrak.scalautils.validating.ErrorReports.{ValidatedER, ValidationContext}

import scala.xml.NodeSeq
import optrak.lagomtest.utils.{CsvSerializer, XmlSerializer}
import optrak.scalautils.data.common.HeaderBuilder
import optrak.scalautils.data.common.Headers.{BuiltInInputHeaders, BuiltInOutputHeaders}
import optrak.scalautils.data.common.Writing.CellWriter
import optrak.scalautils.data.csv.{CsvCellParser, CsvCellWriter, CsvRow, CsvRowWriter}
import optrak.scalautils.xml.{XmlParser, XmlWriter}
import optrak.lagomtest.utils.CheckedDoneSerializer._
import scala.collection.immutable

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  */
trait VehicleService extends Service {

  type Vehicles = List[Vehicle]

  def updateCapacity(tenant: TenantId, id: VehicleId, newCapacity: Int): ServiceCall[NotUsed, Done]

  def getVehicle(tenant: TenantId, id: VehicleId): ServiceCall[NotUsed, Vehicle]

  def getVehiclesForTenant(tenant: TenantId): ServiceCall[NotUsed, VehicleIds]

  implicit val vehiclesSerializer: StrictMessageSerializer[Vehicles] = {
    import optrak.scalautils.data.csv.CsvImplicits._
    
    val csvParser = CsvCellParser[Vehicle]
    val csvCellWriter = CsvCellWriter[Vehicle]
    val csvRowWriter = new CsvRowWriter[Vehicle] {
      override def cellWriter: (Vehicle) => CellWriter[Vehicle, String, String, CsvRow, CsvRow] = (_ => csvCellWriter)
    }
    val headerBuilder = HeaderBuilder[Vehicle]
    val inputHeaders = BuiltInInputHeaders // ie uses case class field names
    val outputHeaders = BuiltInOutputHeaders

    CsvSerializer.csvFormatMessageSerializer[Vehicle](
      CsvSerializer.CsvMessageSerializer,
      csvParser,
      csvRowWriter,
      headerBuilder,
      inputHeaders,
      outputHeaders
    )

  }

  def createVehiclesFromCsv(tenantId: TenantId): ServiceCall[Vehicles, CheckedDone]

  implicit val xmlVehicleCreationSerializer: StrictMessageSerializer[VehicleCreationData] = {
    import optrak.scalautils.xml.XmlImplicits._
    val vcdParser = XmlParser[VehicleCreationData]
    val vcdWriter = XmlWriter[VehicleCreationData]

    val xmlS = XmlSerializer.XmlMessageSerializer

    XmlSerializer.xmlFormatMessageSerializer[VehicleCreationData](xmlS, vcdParser, vcdWriter)
  }


  def createVehicleXml(tenant: TenantId, id: VehicleId): ServiceCall[VehicleCreationData, Done]

  override final def descriptor = {
    import Service._

    named("vehicle").withCalls(
      pathCall("/optrak.lagom.vehicles.api/:tenant/capacity/:id/:newCapacity", updateCapacity _),
      pathCall("/optrak.lagom.vehicles.api/:tenant/create/:id", createVehicleXml _),
      pathCall("/optrak.lagom.vehicles.api/:tenant/createFromCsv", createVehiclesFromCsv _),
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



