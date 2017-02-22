package optrak.lagomtest.sites.impl

import java.util.concurrent.atomic.AtomicInteger

import akka.persistence.query.Sequence
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server.LagomApplication
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import optrak.lagomtest.data.Data.SiteId
import optrak.lagomtest.sites.impl.SiteEvents.{SiteCreated, SiteEvent}
import optrak.lagomtest.utils.ReadSideTestDriver
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.libs.ws.ahc.AhcWSComponents
import SiteTestCommon._
import optrak.lagomtest.sites.api.SiteIds

import scala.concurrent.Future


class SiteRepositoryTest extends AsyncWordSpec with BeforeAndAfterAll with Matchers {

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
    new LagomApplication(ctx) with SiteComponents with AhcWSComponents {
      override def serviceLocator = NoServiceLocator
      override lazy val readSide: ReadSideTestDriver = new ReadSideTestDriver
    }
  }

  override def afterAll() = server.stop()

  private val testDriver = server.application.readSide
  private val siteRepository = server.application.siteRepository
  private val offset = new AtomicInteger()


  "The site event processor" should {
    "create a site" in {
      val siteCreated = SiteCreated(tenantId, site1Id, postcode1)
      for {
        _ <- feed(entityId(tenantId, site1Id), siteCreated)
        sites <- getSites
      } yield {
        sites.ids should contain only siteCreated.siteId
      }
    }

    "create another site" in {
      val siteCreated = SiteCreated(tenantId, site2Id, postcode2)
      for {
        _ <- feed(entityId(tenantId, site2Id), siteCreated)
        allSites <- getSites
      } yield {
        allSites.ids should contain (site1Id)
      }
    }
  }

  private def getSites = {
    siteRepository.selectSitesForTenant(tenantId)
  }

  private def feed(siteId: SiteId, event: SiteEvent) = {
    testDriver.feed(siteId.toString, event, Sequence(offset.getAndIncrement))
  }


}
