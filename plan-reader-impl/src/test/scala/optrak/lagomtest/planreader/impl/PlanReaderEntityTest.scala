package optrak.lagomtest.planreader.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import optrak.lagomtest.data.Data._
import optrak.lagomtest.plan.PlanSerializerRegistry
import optrak.lagomtest.plan.api.PlanEvents.{PlanCreated, PlanEvent, ProductRemoved, ProductUpdated}
import optrak.lagomtest.plan.api.PlanImpl
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}

import scala.collection.immutable.Seq

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class PlanReaderEntityTest extends WordSpec with Matchers with BeforeAndAfterAll with OptionValues {
  object TestRegistry extends JsonSerializerRegistry {

    override def serializers: Seq[JsonSerializer[_]] = PlanReaderSerializerRegistry.serializers ++ PlanSerializerRegistry.serializers
  }

  private val system = ActorSystem("TenantEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(TestRegistry))

//  private val system = ActorSystem("PlanReaderEntitySpec", setups)

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  val planId = UUIDs.timeBased()
  val hello = "hellow"
  val planDescription = PlanDescription(planId, hello)

  val product1 = Product("product1", 1, "g1", false)
  val product2 = Product("product2", 2, "g1", false)

  private def withTestDriver[T](block: PersistentEntityTestDriver[PlanReaderCommand, PlanEvent, Option[PlanImpl]] => T): T= {
    val driver = new PersistentEntityTestDriver(system, new PlanReaderEntity, planId.toString)
    try {
      block(driver)
    } finally {
      driver.getAllIssues shouldBe empty
    }
  }

  "Tenant entity" should {

    "allow creation of plan" in withTestDriver { driver =>
      val outcome = driver.run(WrappedPlanEvent(PlanCreated(planDescription)))
      outcome.replies === Vector(Done)
      outcome.events should contain(PlanCreated(planDescription))
      outcome.state === Some(PlanImpl(planDescription))
    }

    "add product" in withTestDriver { driver =>
      val outcome1 = driver.run(WrappedPlanEvent(PlanCreated(planDescription)))
      val outcome = driver.run(WrappedPlanEvent(ProductUpdated(planId, product1)))
      outcome.replies === Vector(Done)
      outcome.events should contain(ProductUpdated(planId, product1))
      outcome.state === Some(PlanImpl(PlanDescription(planId, hello), productsM = Map(product1.id -> product1)))
    }

    "remove product " in withTestDriver { driver =>
      val outcome1 = driver.run(WrappedPlanEvent(PlanCreated(planDescription)))
      val outcome2 = driver.run(WrappedPlanEvent(ProductUpdated(planId, product1)))

      val outcome = driver.run(WrappedPlanEvent(ProductRemoved(planId, product1.id)))
      outcome.replies === Vector(Done)
      outcome.events should contain(ProductRemoved(planId, product1.id))
      outcome.state === Some(PlanImpl(planDescription))
    }
    // todo =- all the other tests

  }
}
