package optrak.lagomtest.plan.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import optrak.lagomtest.data.Data._
import optrak.lagomtest.plan.PlanCommands.{AddOrUpdateProduct, CreatePlan, PlanCommand, RemoveProduct}
import optrak.lagomtest.plan.{PlanEntity, PlanSerializerRegistry}
import optrak.lagomtest.plan.api.PlanEvents.{PlanCreated, PlanEvent, ProductRemoved, ProductUpdated}
import optrak.lagomtest.plan.api.PlanImpl
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class PlanEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll with OptionValues {

  private val system = ActorSystem("TenantEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(PlanSerializerRegistry))

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  val planId = UUIDs.timeBased()
  val hello = "hellow"
  val planDescription = PlanDescription(planId, hello)

  val product1 = Product("product1", 1, "g1",false)
  val product2 = Product("product2", 2, "g1", false)

  private def withTestDriver[T](block: PersistentEntityTestDriver[PlanCommand, PlanEvent, Option[PlanImpl]] => T): T= {
    val driver = new PersistentEntityTestDriver(system, new PlanEntity, planId.toString)
    try {
      block(driver)
    } finally {
      driver.getAllIssues shouldBe empty
    }
  }

  "Tenant entity" should {

    "allow creation of plan" in withTestDriver { driver =>
      val outcome = driver.run(CreatePlan(planDescription))
      outcome.replies === Vector(Done)
      outcome.events should contain(PlanCreated(planDescription))
      outcome.state === Some(PlanImpl(planDescription))
    }
  }

  "Products" should {

    "add with addOrUpdate product" in withTestDriver { driver =>
      val outcome1 = driver.run(CreatePlan(planDescription))
      val outcome = driver.run(AddOrUpdateProduct(planId, product1))
      outcome.replies === Vector(Done)
      outcome.events should contain(ProductUpdated(planId, product1))
      outcome.state === Some(PlanImpl(PlanDescription(planId, hello), productsM = Map(product1.id -> product1)))
    }

    "remove product " in withTestDriver { driver =>
      val outcome1 = driver.run(CreatePlan(planDescription))
      val outcome2 = driver.run(AddOrUpdateProduct(planId, product1))

      val outcome = driver.run(RemoveProduct(planId, product1.id))
      outcome.replies === Vector(Done)
      outcome.events should contain(ProductRemoved(planId, product1.id))
      outcome.state === Some(PlanImpl(planDescription))
    }

  }
}
