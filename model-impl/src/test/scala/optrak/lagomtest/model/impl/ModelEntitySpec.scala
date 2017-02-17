package optrak.lagomtest.model.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import optrak.lagomtest.datamodel.Models._
import optrak.lagomtest.datamodel.impl.{ModelEntity, ModelSerializerRegistry}
import ModelCommands._
import optrak.lagomtest.model.api.ModelEvents._
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ModelEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll with OptionValues {

  private val system = ActorSystem("TenantEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(ModelSerializerRegistry))

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  val modelId = UUIDs.timeBased()
  val hello = "hellow"
  val modelDescription = ModelDescription(modelId, hello)

  val product1 = Product("product1", 1, "g1",false)
  val product2 = Product("product2", 2, "g1", false)

  private def withTestDriver[T](block: PersistentEntityTestDriver[ModelCommand, ModelEvent, Option[Model]] => T): T= {
    val driver = new PersistentEntityTestDriver(system, new ModelEntity, modelId.toString)
    try {
      block(driver)
    } finally {
      driver.getAllIssues shouldBe empty
    }
  }

  "Tenant entity" should {

    "allow creation of model" in withTestDriver { driver =>
      val outcome = driver.run(CreateModel(modelDescription))
      outcome.replies === Vector(Done)
      outcome.events should contain(ModelCreated(modelDescription))
      outcome.state === Some(Model(modelDescription))
    }

    "add product" in withTestDriver { driver =>
      val outcome1 = driver.run(CreateModel(modelDescription))
      val outcome = driver.run(AddOrUpdateProduct(modelId, product1))
      outcome.replies === Vector(Done)
      outcome.events should contain(ProductUpdated(modelId, product1))
      outcome.state === Some(Model(modelId, hello, Map(product1.id -> product1)))
    }

    "remove product " in withTestDriver { driver =>
      val outcome1 = driver.run(CreateModel(modelDescription))
      val outcome2 = driver.run(AddOrUpdateProduct(modelId, product1))

      val outcome = driver.run(RemoveProduct(modelId, product1.id))
      outcome.replies === Vector(Done)
      outcome.events should contain(ProductRemoved(modelId, product1.id))
      outcome.state === Some(Model(modelDescription))
    }
    // todo =- all the other tests

  }
}
