package optrak.lagomtest.modelreader.impl

import akka.Done
import akka.actor.ActorSystem
import akka.actor.setup.ActorSystemSetup
import akka.serialization.SerializationSetup
import akka.testkit.TestKit
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry.{serializationSetupFor, serializerDetailsFor}
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import optrak.lagomtest.datamodel.Models._
import optrak.lagomtest.datamodel.impl.ModelSerializerRegistry
import optrak.lagomtest.model.api.ModelEvents._
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}

import scala.collection.immutable.Seq

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ModelReaderEntityTest extends WordSpec with Matchers with BeforeAndAfterAll with OptionValues {
  object TestRegistry extends JsonSerializerRegistry {

    override def serializers: Seq[JsonSerializer[_]] = ModelReaderSerializerRegistry.serializers ++ ModelSerializerRegistry.serializers
  }

  private val system = ActorSystem("ClientEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(TestRegistry))

//  private val system = ActorSystem("ModelReaderEntitySpec", setups)

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  val modelId = UUIDs.timeBased()
  val hello = "hellow"
  val modelDescription = ModelDescription(modelId, hello)

  val product1 = Product("product1", 1, "g1", false)
  val product2 = Product("product2", 2, "g1", false)

  private def withTestDriver[T](block: PersistentEntityTestDriver[ModelReaderCommand, ModelEvent, Option[Model]] => T): T= {
    val driver = new PersistentEntityTestDriver(system, new ModelReaderEntity, modelId.toString)
    try {
      block(driver)
    } finally {
      driver.getAllIssues shouldBe empty
    }
  }

  "Client entity" should {

    "allow creation of model" in withTestDriver { driver =>
      val outcome = driver.run(WrappedModelEvent(ModelCreated(modelDescription)))
      outcome.replies === Vector(Done)
      outcome.events should contain(ModelCreated(modelDescription))
      outcome.state === Some(Model(modelDescription))
    }

    "add product" in withTestDriver { driver =>
      val outcome1 = driver.run(WrappedModelEvent(ModelCreated(modelDescription)))
      val outcome = driver.run(WrappedModelEvent(ProductUpdated(modelId, product1)))
      outcome.replies === Vector(Done)
      outcome.events should contain(ProductUpdated(modelId, product1))
      outcome.state === Some(Model(modelId, hello, Map(product1.id -> product1)))
    }

    "remove product " in withTestDriver { driver =>
      val outcome1 = driver.run(WrappedModelEvent(ModelCreated(modelDescription)))
      val outcome2 = driver.run(WrappedModelEvent(ProductUpdated(modelId, product1)))

      val outcome = driver.run(WrappedModelEvent(ProductRemoved(modelId, product1.id)))
      outcome.replies === Vector(Done)
      outcome.events should contain(ProductRemoved(modelId, product1.id))
      outcome.state === Some(Model(modelDescription))
    }
    // todo =- all the other tests

  }
}
