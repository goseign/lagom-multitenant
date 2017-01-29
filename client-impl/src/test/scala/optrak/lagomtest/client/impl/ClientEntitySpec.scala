package optrak.lagomtest.client.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import optrak.lagomtest.client.impl.ClientEvents.{ClientCreated, ClientEvent, ModelCreated, ModelRemoved}
import optrak.lagomtest.client.api.{ModelCreated => ApiModelCreated}
import optrak.lagomtest.datamodel.Models.{Client, ModelDescription}
import org.specs2.matcher.MatchResult
import org.specs2.mutable.{BeforeAfter, Specification}
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ClientEntitySpec extends Specification with BeforeAfter {

  private val system = ActorSystem("ClientEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(ClientSerializerRegistry))

  override def after: Any = {
    TestKit.shutdownActorSystem(system)
  }

  val clientId = "client1"
  val modelId = UUIDs.timeBased()

  override def before: Any = {}

  private def withTestDriver(block: PersistentEntityTestDriver[ClientCommand, ClientEvent, Option[Client]] => MatchResult[_]): MatchResult[_] = {
    val driver = new PersistentEntityTestDriver(system, new ClientEntity, clientId)
    block(driver)
    driver.getAllIssues should beEmpty
  }

  "Client entity" should {

    "allow creation of Client" in withTestDriver { driver =>
      val outcome = driver.run(CreateClient(clientId, "hello"))
      outcome.replies === Vector(Done)
      outcome.events should contain(ClientCreated(clientId, "hello"))
      outcome.state === Some(Client(clientId, Set.empty, "hello"))
    }

    "create model" in withTestDriver { driver =>
      val outcome1 = driver.run(CreateClient(clientId, "hello"))
      val outcome = driver.run(CreateModel(modelId, "nice model"))
      outcome.replies === Vector(ApiModelCreated(modelId))
      outcome.events should contain(ModelCreated(modelId, "nice model"))
      outcome.state === Some(Client(clientId, Set(ModelDescription(modelId, "nice model")), "hello"))
    }

    "remove model" in withTestDriver { driver =>
      val outcome1 = driver.run(CreateClient(clientId, "hello"))
      val outcome2 = driver.run(CreateModel(modelId, "nice model"))

      val outcome = driver.run(RemoveModel(modelId))
      outcome.replies === Vector(Done)
      outcome.events should contain(ModelRemoved(modelId))
      outcome.state === Some(Client(clientId, Set.empty, "hello"))
    }

    "remove model twice does not complain" in withTestDriver { driver =>
      val outcome1 = driver.run(CreateClient(clientId, "hello"))
      val outcome2 = driver.run(CreateModel(modelId, "nice model"))

      val outcome3 = driver.run(RemoveModel(modelId))
      val outcome = driver.run(RemoveModel(modelId))
      outcome.replies === Vector(Done)
      outcome.events should contain(ModelRemoved(modelId))
      outcome.state === Some(Client(clientId, Set.empty, "hello"))
    }



  }
}
