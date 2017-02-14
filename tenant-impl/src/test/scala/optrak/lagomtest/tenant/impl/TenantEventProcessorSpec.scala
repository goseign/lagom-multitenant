package optrak.lagomtest.tenant.impl

import java.util.concurrent.atomic.AtomicInteger

import akka.persistence.query.Sequence
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server.LagomApplication
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import TenantEvents.{TenantCreated, TenantEvent}
import optrak.lagomtest.datamodel.Models.TenantId
import optrak.lagomtest.utils.ReadSideTestDriver
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.libs.ws.ahc.AhcWSComponents

class TenantEventProcessorSpec extends AsyncWordSpec with BeforeAndAfterAll with Matchers {

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
    new LagomApplication(ctx) with TenantComponents with AhcWSComponents {
      override def serviceLocator = NoServiceLocator
      override lazy val readSide: ReadSideTestDriver = new ReadSideTestDriver
    }
  }

  override def afterAll() = server.stop()

  private val testDriver = server.application.readSide
  private val clientRepository = server.application.clientRepository
  private val offset = new AtomicInteger()


  "The client event processor" should {
    "create a client" in {
      val clientCreated = TenantCreated("tim", "hello")
      for {
        _ <- feed(clientCreated.id, clientCreated)
        clients <- getTenants
      } yield {
        clients should contain only clientCreated.id
      }
    }

  }

  private def getTenants = {
    clientRepository.selectAllTenants
  }

  private def feed(clientId: TenantId, event: TenantEvent) = {
    testDriver.feed(clientId.toString, event, Sequence(offset.getAndIncrement))
  }
}