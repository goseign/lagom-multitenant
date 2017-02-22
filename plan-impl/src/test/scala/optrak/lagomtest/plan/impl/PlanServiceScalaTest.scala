package optrak.lagomtest.plan.impl

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
import akka.Done
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import optrak.lagomtest.data.Data.Product
import optrak.lagomtest.plan.PlanApplication
import optrak.lagomtest.plan.api.PlanService
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class PlanServiceScalaTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new PlanApplication(ctx) with LocalServiceLocator
  }

  val planService = server.serviceClient.implement[PlanService]
  val product1 = Product("product1", 1, "g1", false)

  override protected def afterAll() = server.stop()

  val planId = UUIDs.timeBased()


  "plan service" should {

    "create plan" in {
      for {
        answer <- planService.createPlan(planId).invoke("my plan")
      } yield {
        answer should ===(Done)
      }
    }

    "add product" in {
      for {
        answer <- planService.addProduct(planId).invoke(product1)
      } yield {
        answer should === (Done)
      }
    }

    "removed plan" in {
      for {
        answer <- planService.removeProduct(planId).invoke(product1.id)
      } yield {
        answer should === (Done)
      }
    }
  }
}
