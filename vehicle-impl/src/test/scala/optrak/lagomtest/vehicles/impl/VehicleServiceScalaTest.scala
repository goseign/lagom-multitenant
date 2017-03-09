package optrak.lagomtest.vehicles.impl

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
import akka.Done
import com.lightbend.lagom.scaladsl.api.deser.StrictMessageSerializer
import com.lightbend.lagom.scaladsl.api.transport.TransportException
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import optrak.lagom.utils.CheckedDoneSerializer.CheckedDone
import optrak.lagomtest.data.Data.{TenantId, Vehicle, VehicleId}
import optrak.lagomtest.vehicles.api.VehicleService.Vehicles
import optrak.lagomtest.vehicles.api.{VehicleCreationData, VehicleService}
import optrak.lagomtest.vehicles.impl.VehicleTestCommon._
import optrak.scalautils.validating.ErrorReports.EitherER
import org.scalacheck._
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import org.scalacheck.Shapeless._
import optrak.scalautils.validating.ErrorReports.ErrorReport._

import scala.concurrent.Future

class VehicleServiceScalaTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new VehicleApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[VehicleService]

  override protected def afterAll() = server.stop()
  
  def createVehicleData(vehicle: Vehicle) = VehicleCreationData(vehicle.capacity)

  "vehicle service" should {

    "create and retrieve vehicle with xml" in {

      for {
        answer <- client.createVehicleXml(tenantId, vehicle1Id).invoke(createVehicleData(vehicle1))
    //    retrieved <- client.getVehicle(tenantId, vehicle1Id).invoke()
      } yield {
        answer should ===(Done)
      }
    }

    "complain about 2nd attempt create vehicle" in {
      val exp = recoverToExceptionIf[TransportException](
      for {
        answer <- client.createVehicleXml(tenantId, vehicle1Id).invoke(createVehicleData(vehicle1))
        answer2 <- client.createVehicleXml(tenantId, vehicle1Id).invoke(createVehicleData(vehicle1))
      } yield {
        answer2 should ===(Done)
      })
      exp.map { te =>
        // println(s"te is code ${te.errorCode} message ${te.exceptionMessage}")
        te.toString should include("vehicle vehicle1 for tenant tenant1 already exists")
      }
    }


  }


  "reading" should {
    def createP(implicit arb: Arbitrary[VehicleCreationData]): Option[VehicleCreationData] =
      arb.arbitrary.sample

   "create multiple vehicles for single tenant" in {

      implicitly[Arbitrary[VehicleCreationData]]

      // we're going to generate some arbitrary vehicles then check that they actually got created
      val cps : List[VehicleCreationData] = 0.to(2).flatMap( i => createP ).toList
      val vehicles: List[(VehicleId, VehicleCreationData)] = cps.zipWithIndex.map(t => (t._2.toString, t._1))
      val vehiclesCreated = vehicles.map {t =>
        Thread.sleep(1500)
        client.createVehicleXml(tenantId, t._1).invoke(t._2)
      }

      for {
        seq <- Future.sequence(vehiclesCreated)
        dbAllVehicles <- {
          Thread.sleep(4000)
          client.getVehiclesForTenant(tenantId).invoke()
        }


      } yield {
        val ap = dbAllVehicles
        println(s"all vehicles is $dbAllVehicles")

        vehicles.foreach { p =>
          val found = ap.ids.find(_ == p._1)
          found should === (Some(p._1))
        }

        true should ===(true)

      }
    }

    def createVehicles: List[Vehicle] = {
      // we're going to generate some arbitrary vehicles then check that they actually got created
      val cps : List[VehicleCreationData] = 0.to(10).flatMap( i => createP ).toList
      cps.zipWithIndex.map(t => Vehicle(t._2.toString, t._1.capacity))
    }

    def checkVehicles(storing: Future[CheckedDone],
                      vehicles: List[Vehicle],
                      tenantId: TenantId)
                     (implicit serializer: StrictMessageSerializer[EitherER[Vehicles]]) =
      for {
        stored <- storing
        dbAllVehicles <- {
          Thread.sleep(4000)
          client.getVehiclesForTenant(tenantId).invoke()
        }


      } yield {
        val ap = dbAllVehicles
        println(s"all vehicles is $dbAllVehicles")

        vehicles.foreach { p =>
          val found = ap.ids.find(_ == p.id)
          found should === (Some(p.id))
        }

        true should ===(true)

      }


    "create multiple vehicles with csv" in {

      val tenantId = "csvTenant"
      val vehicles = createVehicles
      val storing = client.createVehiclesFromCsv(tenantId).invoke(Right(vehicles))
      checkVehicles(storing, vehicles, tenantId)(client.vehiclesCsvSerializer)
    }

    "create multiple vehicles with xls" in {

      val tenantId = "xlsTenant"
      val vehicles = createVehicles
      val storing = client.createVehiclesFromXls(tenantId).invoke(Right(vehicles))
      checkVehicles(storing, vehicles, tenantId)(client.vehiclesXlsSerializer)
    }


  }


}
