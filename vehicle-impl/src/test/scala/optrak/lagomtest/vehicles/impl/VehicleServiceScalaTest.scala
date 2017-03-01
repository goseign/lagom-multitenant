package optrak.lagomtest.vehicles.impl

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.TransportException
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import optrak.lagomtest.data.Data.{Vehicle, VehicleId}
import optrak.lagomtest.vehicles.api.{VehicleCreationData, VehicleService}
import optrak.lagomtest.vehicles.impl.VehicleTestCommon._
import org.scalacheck._
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import org.scalacheck.Shapeless._

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

  def createP(implicit arb: Arbitrary[VehicleCreationData]): Option[VehicleCreationData] =
    arb.arbitrary.sample

  def createV(implicit arb: Arbitrary[Vehicle]): Option[Vehicle] =
    arb.arbitrary.sample


  "vehicle service" should {

    "create and retrieve vehicle with xml" in {

      for {
        answer <- client.createVehicleXml(tenantId, vehicle1Id).invoke(createVehicleData(vehicle1))
        retrieved <- client.getVehicle(tenantId, vehicle1Id).invoke()
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

    "create as csv upload" in {
      val vehicles : List[Vehicle] = 0.to(10).flatMap( i => createV ).toList
      for {
        answer <- client.createVehiclesFromCsv(tenantId).invoke(vehicles)
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


    }


  }


 /* "reading" should {

    "create multiple vehicles for single tenant" in {

      implicitly[Arbitrary[VehicleCreationData]]

      // we're going to generate some arbitrary vehicles then check that they actually got created
      val cps : List[VehicleCreationData] = 0.to(10).flatMap( i => createP ).toList
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
    }
*/


}
