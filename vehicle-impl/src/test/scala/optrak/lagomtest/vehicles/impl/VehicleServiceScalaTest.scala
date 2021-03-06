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


  "vehicle service" should {

    "create and retrieve vehicle" in {
      for {
        answer <- client.createVehicle(tenantId, vehicle1Id).invoke(createVehicleData(vehicle1))
        retrieved <- client.getVehicle(tenantId, vehicle1Id).invoke()
      } yield {
        answer should ===(Done)
      }
    }


    "complain about 2nd attempt create vehicle" in {
      val exp = recoverToExceptionIf[TransportException](
      for {
        answer <- client.createVehicle(tenantId, vehicle1Id).invoke(createVehicleData(vehicle1))
        answer2 <- client.createVehicle(tenantId, vehicle1Id).invoke(createVehicleData(vehicle1))
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
      val cps : List[VehicleCreationData] = 0.to(10).flatMap( i => createP ).toList
      val vehicles: List[(VehicleId, VehicleCreationData)] = cps.zipWithIndex.map(t => (t._2.toString, t._1))
      val vehiclesCreated = vehicles.map {t =>
        Thread.sleep(1500)
        client.createVehicle(tenantId, t._1).invoke(t._2)
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

}
