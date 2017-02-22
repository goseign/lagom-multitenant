package optrak.lagomtest.tenant.impl

import java.util.concurrent.atomic.AtomicInteger

import akka.persistence.query.Sequence
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server.LagomApplication
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import TenantEvents.{TenantCreated, TenantEvent}
import optrak.lagomtest.data.Data.TenantId
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
  private val tenantRepository = server.application.tenantRepository
  private val offset = new AtomicInteger()


  "The tenant event processor" should {
    "create a tenant" in {
      val tenantCreated = TenantCreated("tim", "hello")
      for {
        _ <- feed(tenantCreated.id, tenantCreated)
        tenants <- getTenants
      } yield {
        tenants should contain only tenantCreated.id
      }
    }

  }

  private def getTenants = {
    tenantRepository.selectAllTenants
  }

  private def feed(tenantId: TenantId, event: TenantEvent) = {
    testDriver.feed(tenantId.toString, event, Sequence(offset.getAndIncrement))
  }
}