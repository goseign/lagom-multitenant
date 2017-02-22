package optrak.lagomtest.plan.impl

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
import akka.Done
import akka.stream.scaladsl.Sink
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.{ServiceTest, TestTopicComponents}
import optrak.lagomtest.data.Data.{PlanDescription, Product}
import optrak.lagomtest.plan.PlanApplication
import optrak.lagomtest.plan.api.PlanEvents.{PlanCreated, ProductRemoved, ProductUpdated}
import optrak.lagomtest.plan.api.PlanService
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class PlanEventStreamTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new PlanApplication(ctx) with LocalServiceLocator with TestTopicComponents
  }

  val planService = server.serviceClient.implement[PlanService]
  val product1 = Product("product1", 1, "g1", false)

  override protected def afterAll() = server.stop()

  val planId = UUIDs.timeBased()

  implicit val mat = server.materializer

  "plan service" should {

    "create plan" in {
      for {
        answer <- planService.createPlan(planId).invoke("my plan")
        event <- planService.planEvents.subscribe.atMostOnceSource
          .runWith(Sink.head)
      } yield {
        answer should === (Done)
        event should === (PlanCreated(PlanDescription(planId, "my plan")))
      }
    }

    "add product" in {
      for {
        answer <- planService.addProduct(planId).invoke(product1)
        event <- planService.planEvents.subscribe.atMostOnceSource.take(2).runWith(Sink.last)
      } yield {
        answer should === (Done)
        event should === (ProductUpdated(planId, product1))
      }
    }

    "removed plan" in {
      for {
        answer <- planService.removeProduct(planId).invoke(product1.id)
        event <- planService.planEvents.subscribe.atMostOnceSource.take(3).runWith(Sink.last)
      } yield {
        answer should === (Done)
        event should === (ProductRemoved(planId, product1.id))
      }
    }
  }
}
