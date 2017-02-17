package optrak.lagomtest.tenant.impl

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.TransportException
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import optrak.lagomtest.tenant.api.{ModelCreated, ModelCreationData, TenantCreationData, TenantService}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class TenantServiceScalaTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new TenantApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[TenantService]

  override protected def afterAll() = server.stop()

  val tenantId = "tim"


  "tenant service" should {

    "create tenantc" in {
      for {
        answer <- client.createTenant(tenantId).invoke(TenantCreationData("my tenant"))
      } yield {
        answer should ===(Done)
      }
    }

    "complain about 2nd attempt create tenant" in {
      val exp = recoverToExceptionIf[TransportException](
      for {
        answer <- client.createTenant(tenantId).invoke(TenantCreationData("my tenant"))
        answer2 <- client.createTenant(tenantId).invoke(TenantCreationData("my tenant"))
      } yield {
        answer2 should ===(Done)
      })
      exp.map { te =>
        // println(s"te is code ${te.errorCode} message ${te.exceptionMessage}")
        te.toString should include("tim already exists")
      }
    }

    "removed model" in {
      for {
        added <- client.createModel(tenantId).invoke(ModelCreationData("nice model"))
        removed <- client.removeModel(tenantId, added.id).invoke()
      } yield {
        added shouldBe a [ModelCreated]

        removed should === (Done)
      }
    }
  }
}
