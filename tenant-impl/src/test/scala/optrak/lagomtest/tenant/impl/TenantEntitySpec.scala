package optrak.lagomtest.tenant.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import TenantEvents.{TenantCreated, TenantEvent, ModelCreated, ModelRemoved}
import optrak.lagomtest.data.Data.{Tenant, PlanDescription}
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}
import optrak.lagomtest.tenant.api.{ModelCreated => ApiModelCreated}
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class TenantEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll with OptionValues {

  private val system = ActorSystem("TenantEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(TenantSerializerRegistry))

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  val tenantId = "tenant1"
  val modelId = UUIDs.timeBased()

  private def withTestDriver[T](block: PersistentEntityTestDriver[TenantCommand, TenantEvent, Option[Tenant]] => T): T = {
    val driver = new PersistentEntityTestDriver(system, new TenantEntity, tenantId)
    try {
      block(driver)
    } finally {
      driver.getAllIssues shouldBe empty
    }
  }

  "Tenant entity" should {

    "allow creation of Tenant" in withTestDriver { driver =>
      val outcome = driver.run(CreateTenant(tenantId, "hello"))
      outcome.replies === Vector(Done)
      outcome.events should contain(TenantCreated(tenantId, "hello"))
      outcome.state === Some(Tenant(tenantId, Set.empty, "hello"))
    }

    "create model" in withTestDriver { driver =>
      val outcome1 = driver.run(CreateTenant(tenantId, "hello"))
      val outcome = driver.run(CreateModel(modelId, "nice model"))
      outcome.replies === Vector(ApiModelCreated(modelId))
      outcome.events should contain(ModelCreated(modelId, "nice model"))
      outcome.state === Some(Tenant(tenantId, Set(PlanDescription(modelId, "nice model")), "hello"))
    }


    "remove model" in withTestDriver { driver =>
      val outcome1 = driver.run(CreateTenant(tenantId, "hello"))
      val outcome2 = driver.run(CreateModel(modelId, "nice model"))

      val outcome = driver.run(RemoveModel(modelId))
      outcome.replies === Vector(Done)
      outcome.events should contain(ModelRemoved(modelId))
      outcome.state === Some(Tenant(tenantId, Set.empty, "hello"))
    }

    "remove model twice does not complain" in withTestDriver { driver =>
      val outcome1 = driver.run(CreateTenant(tenantId, "hello"))
      val outcome2 = driver.run(CreateModel(modelId, "nice model"))

      val outcome3 = driver.run(RemoveModel(modelId))
      val outcome = driver.run(RemoveModel(modelId))
      outcome.replies === Vector(Done)
      outcome.events should contain(ModelRemoved(modelId))
      outcome.state === Some(Tenant(tenantId, Set.empty, "hello"))
    }



  }
}
