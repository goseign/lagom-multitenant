package optrak.lagomtest.vehicles.impl

import java.util.concurrent.atomic.AtomicInteger

import akka.persistence.query.Sequence
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server.LagomApplication
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import optrak.lagomtest.data.Data.VehicleId
import optrak.lagom.utils .ReadSideTestDriver
import optrak.lagomtest.vehicles.impl.VehicleEvents.{VehicleCreated, VehicleEvent}
import optrak.lagomtest.vehicles.impl.VehicleTestCommon._
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.libs.ws.ahc.AhcWSComponents


class VehicleRepositoryTest extends AsyncWordSpec with BeforeAndAfterAll with Matchers {

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
    new LagomApplication(ctx) with VehicleComponents with AhcWSComponents {
      override def serviceLocator = NoServiceLocator
      override lazy val readSide: ReadSideTestDriver = new ReadSideTestDriver
    }
  }

  override def afterAll() = server.stop()

  private val testDriver = server.application.readSide
  private val vehicleRepository = server.application.vehicleRepository
  private val offset = new AtomicInteger()


  "The vehicle event processor" should {
    "create a vehicle" in {
      val vehicleCreated = VehicleCreated(tenantId, vehicle1Id, capacity1)
      for {
        _ <- feed(entityId(tenantId, vehicle1Id), vehicleCreated)
        vehicles <- getVehicles
      } yield {
        vehicles.ids should contain only vehicleCreated.vehicleId
      }
    }

    "create another vehicle" in {
      val vehicleCreated = VehicleCreated(tenantId, vehicle2Id, capacity2)
      for {
        _ <- feed(entityId(tenantId, vehicle2Id), vehicleCreated)
        allVehicles <- getVehicles
      } yield {
        allVehicles.ids should contain (vehicle1Id)
      }
    }
  }

  private def getVehicles = {
    vehicleRepository.selectVehiclesForTenant(tenantId)
  }

  private def feed(vehicleId: VehicleId, event: VehicleEvent) = {
    testDriver.feed(vehicleId.toString, event, Sequence(offset.getAndIncrement))
  }


}
