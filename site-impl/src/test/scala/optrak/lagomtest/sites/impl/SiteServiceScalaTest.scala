package optrak.lagomtest.sites.impl

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.TransportException
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import optrak.lagomtest.sites.api.{SiteCreationData, SiteService}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import SiteTestCommon._
import optrak.lagomtest.datamodel.Models.{Site, SiteId}
import org.scalacheck._
import org.scalacheck.Shapeless._
// import org.scalacheck.Gen._

import scala.concurrent.Future

class SiteServiceScalaTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new SiteApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[SiteService]

  override protected def afterAll() = server.stop()
  
  def createSiteData(site: Site) = SiteCreationData(site.postcode)


  "site service" should {

    "create and retrieve site" in {
      for {
        answer <- client.createSite(tenantId, site1Id).invoke(createSiteData(site1))
        retrieved <- client.getSite(tenantId, site1Id).invoke()
      } yield {
        answer should ===(Done)
      }
    }


    "complain about 2nd attempt create site" in {
      val exp = recoverToExceptionIf[TransportException](
      for {
        answer <- client.createSite(tenantId, site1Id).invoke(createSiteData(site1))
        answer2 <- client.createSite(tenantId, site1Id).invoke(createSiteData(site1))
      } yield {
        answer2 should ===(Done)
      })
      exp.map { te =>
        // println(s"te is code ${te.errorCode} message ${te.exceptionMessage}")
        te.toString should include("site site1 for tenant tenant1 already exists")
      }
    }

  }
  "reading" should {
    def createP(implicit arb: Arbitrary[SiteCreationData]): Option[SiteCreationData] =
      arb.arbitrary.sample

    "create multiple sites for single tenant" in {

      implicitly[Arbitrary[SiteCreationData]]

      // we're going to generate some arbitrary sites then check that they actually got created
      val cps : List[SiteCreationData] = 0.to(10).flatMap( i => createP ).toList
      val sites: List[(SiteId, SiteCreationData)] = cps.zipWithIndex.map(t => (t._2.toString, t._1))
      val sitesCreated = sites.map {t =>
        Thread.sleep(1500)
        client.createSite(tenantId, t._1).invoke(t._2)
      }

      for {
        seq <- Future.sequence(sitesCreated)
        dbAllSites <- {
          Thread.sleep(4000)
          client.getSitesForTenant(tenantId).invoke()
        }


      } yield {
        val ap = dbAllSites
        println(s"all sites is $dbAllSites")

        sites.foreach { p =>
          val found = ap.ids.find(_ == p._1)
          found should === (Some(p._1))
        }

        true should ===(true)

      }
    }

  }

}
