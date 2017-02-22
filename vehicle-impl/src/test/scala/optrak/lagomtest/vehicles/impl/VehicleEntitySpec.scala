package optrak.lagomtest.vehicles.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import optrak.lagomtest.data.Data.Vehicle
import optrak.lagomtest.vehicles.impl.VehicleEvents._
import optrak.lagomtest.vehicles.impl.VehicleTestCommon._
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class VehicleEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll with OptionValues {

  private val system = ActorSystem("VehicleEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(VehicleSerializerRegistry))

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }



  val tenantId = "tenant1"
  val modelId = UUIDs.timeBased()

  private def withTestDriver[T](block: PersistentEntityTestDriver[VehicleCommand, VehicleEvent, Option[Vehicle]] => T): T = {
    val driver = new PersistentEntityTestDriver(system, new VehicleEntity, s"$tenantId:$vehicle1Id")
    try {
      block(driver)
    } finally {
      driver.getAllIssues shouldBe empty
    }
  }

  "Vehicle entity" should {

    "allow creation of Vehicle" in withTestDriver { driver =>
      val outcome = driver.run(createVehicle1)
      outcome.replies === Vector(Done)
      outcome.events should contain(VehicleCreated(tenantId, vehicle1Id, capacity1))
      outcome.state === Some(vehicle1)
    }

    "fail on creation of Vehicle twice" in withTestDriver { driver =>
      val outcome = driver.run(createVehicle1)
      a [VehicleAlreadyExistsException] should be thrownBy driver.run(createVehicle1)
    }


    "get back created vehicle" in withTestDriver { driver =>
      driver.run(createVehicle1)
      val outcome = driver.run(GetVehicle)
      outcome.replies === Vector(vehicle1)
    }


    "change capacity" in withTestDriver { driver =>
      driver.run(createVehicle1)
      driver.run(UpdateVehicleCapacity(tenantId, vehicle1Id, capacity2))
      val outcome = driver.run(GetVehicle)
      outcome.replies === Vector(vehicle1g2)
    }

  }
}
