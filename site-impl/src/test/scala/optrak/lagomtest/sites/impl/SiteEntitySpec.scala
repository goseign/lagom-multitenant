package optrak.lagomtest.sites.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import optrak.lagomtest.data.Data.{PlanDescription, Site}
import optrak.lagomtest.sites.impl.SiteEvents._
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}
import SiteTestCommon._

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class SiteEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll with OptionValues {

  private val system = ActorSystem("SiteEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(SiteSerializerRegistry))

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }



  val tenantId = "tenant1"
  val modelId = UUIDs.timeBased()

  private def withTestDriver[T](block: PersistentEntityTestDriver[SiteCommand, SiteEvent, Option[Site]] => T): T = {
    val driver = new PersistentEntityTestDriver(system, new SiteEntity, s"$tenantId:$site1Id")
    try {
      block(driver)
    } finally {
      driver.getAllIssues shouldBe empty
    }
  }

  "Site entity" should {

    "allow creation of Site" in withTestDriver { driver =>
      val outcome = driver.run(createSite1)
      outcome.replies === Vector(Done)
      outcome.events should contain(SiteCreated(tenantId, site1Id, postcode1))
      outcome.state === Some(site1)
    }

    "fail on creation of Site twice" in withTestDriver { driver =>
      val outcome = driver.run(createSite1)
      a [SiteAlreadyExistsException] should be thrownBy driver.run(createSite1)
    }


    "get back created site" in withTestDriver { driver =>
      driver.run(createSite1)
      val outcome = driver.run(GetSite)
      outcome.replies === Vector(site1)
    }


    "change postcode" in withTestDriver { driver =>
      driver.run(createSite1)
      driver.run(UpdateSitePostcode(tenantId, site1Id, postcode2))
      val outcome = driver.run(GetSite)
      outcome.replies === Vector(site1g2)
    }

  }
}
