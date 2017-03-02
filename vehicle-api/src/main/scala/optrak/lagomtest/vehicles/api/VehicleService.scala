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
import optrak.scalautils.validating.ErrorReports.{EitherER, ValidatedER, ValidationContext}

import scala.xml.NodeSeq
import optrak.lagomtest.utils.{CheckedDoneSerializer, CsvSerializer, XlsSerializer, XmlSerializer}
import optrak.scalautils.data.common.HeaderBuilder
import optrak.scalautils.data.common.Headers.{BuiltInInputHeaders, BuiltInOutputHeaders}
import optrak.scalautils.data.common.Writing.CellWriter
import optrak.scalautils.data.csv.{CsvCellParser, CsvCellWriter, CsvRow, CsvRowWriter}
import optrak.scalautils.xml.{XmlParser, XmlWriter}
import optrak.lagomtest.utils.CheckedDoneSerializer._

import scala.collection.immutable
import MessageSerializer._
import optrak.scalautils.data.poi.{PoiCellParser, PoiCellWriter, PoiRowWriter}
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  */

object VehicleService {
  type Vehicles = List[Vehicle]

}
trait VehicleService extends Service {
  import VehicleService._
  def updateCapacity(tenant: TenantId, id: VehicleId, newCapacity: Int): ServiceCall[NotUsed, Done]

  def getVehicle(tenant: TenantId, id: VehicleId): ServiceCall[NotUsed, Vehicle]

  def getVehiclesForTenant(tenant: TenantId): ServiceCall[NotUsed, VehicleIds]

  val vehiclesCsvSerializer: StrictMessageSerializer[EitherER[Vehicles]] = {
    import optrak.scalautils.data.csv.CsvImplicits._
    
    val csvParser = CsvCellParser[Vehicle]
    val csvCellWriter = CsvCellWriter[Vehicle]
    val csvRowWriter = new CsvRowWriter[Vehicle] {
      override def cellWriter = (_ => csvCellWriter)
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

  val vehiclesXlsSerializer: StrictMessageSerializer[EitherER[Vehicles]] = {
    import optrak.scalautils.data.poi.PoiImplicits._

    val poiParser = PoiCellParser[Vehicle]
    val poiCellWriter = PoiCellWriter[Vehicle]
    val poiRowWriter = new PoiRowWriter[Vehicle] {
      override def cellWriter = (_ => poiCellWriter)
    }
    val headerBuilder = HeaderBuilder[Vehicle]
    val inputHeaders = BuiltInInputHeaders // ie uses case class field names
    val outputHeaders = BuiltInOutputHeaders

    XlsSerializer.poiFormatMessageSerializer[Vehicle](
      XlsSerializer.XlsMessageSerializer,
      poiParser,
      poiRowWriter,
      headerBuilder,
      inputHeaders,
      outputHeaders
    )

  }


  /**
    * Create vehicles from csv file. Note that the deserialization is done within the call mechanism but
    * that parse errors are returned in the CheckedDone message rather than through a transport format
    * error return. This gives us more control over error reporting and logging - for example we could
    * direct the failure messages to a sysop
    * @param tenantId
    */
  def createVehiclesFromERList(tenantId: TenantId): ServiceCall[EitherER[Vehicles], CheckedDone]


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
      pathCall("/optrak.lagom.vehicles.api/:tenant/createFromCsv",
        createVehiclesFromERList _)(vehiclesCsvSerializer,CheckedDoneMessageSerializer),
      pathCall("/optrak.lagom.vehicles.api/:tenant/createFromXls",
        createVehiclesFromERList _)(vehiclesXlsSerializer,CheckedDoneMessageSerializer),
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



