package optrak.lagomtest.planreader.impl

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import optrak.lagomtest.data.Data.{Plan, PlanDescription, Product}
import optrak.lagomtest.plan.api.{PlanImpl, PlanService}
import optrak.lagomtest.planreader.api.PlanReaderService
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class PlanReaderServiceTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new PlanReaderApplication(ctx) with LocalServiceLocator
  }

  val planService = server.serviceClient.implement[PlanService]
  val planReaderService = server.serviceClient.implement[PlanReaderService]
  val product1 = Product("product1", 1, "g1", false)

  override protected def afterAll() = server.stop()

  val planId = UUIDs.timeBased()

  implicit val mat = server.materializer

  "plan service" should {
    "create plan" in {
      for {
        created <- planService.createPlan(planId).invoke("my plan")
        addedProduct <- planService.addProduct(planId).invoke(product1)
        fromReader <- {
          Thread.sleep(2000)
          planReaderService.getPlan(planId).invoke()
        }
      } yield {
        fromReader should ===(PlanImpl(PlanDescription(planId, "my plan"), Map(product1.id -> product1)))
      }
    }
  }
}
