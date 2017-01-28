package optrak.lagomtest.model.impl

import akka.Done
import akka.actor.ActorSystem
import com.datastax.driver.core.utils.UUIDs
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import optrak.lagomtest.model.Models.{Client, ModelDescription}
import optrak.lagomtest.model.impl.ClientEvents.{ClientCreated, ClientEvent, ModelCreated, ModelRemoved}
import org.specs2.matcher.MatchResult
import org.specs2.mutable.{BeforeAfter, Specification}
import optrak.lagomtest.model._
import optrak.lagomtest.model.api.{CreateClient => ApiCreateClient, CreateModel => ApiCreateModel, ModelCreated => ApiModelCreated}
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

  private def withTestDriver(block: PersistentEntityTestDriver[ClientCommand, ClientEvent, ClientState] => MatchResult[_]): MatchResult[_] = {
    val driver = new PersistentEntityTestDriver(system, new ClientEntity, clientId)
    block(driver)
    driver.getAllIssues should beEmpty
  }

  "Client entity" should {

    "allow creation of Client" in withTestDriver { driver =>
      val outcome = driver.run(CreateClient(clientId, "hello"))
      outcome.replies === Vector(Done)
      outcome.events should contain(ClientCreated(clientId, "hello"))
      outcome.state === NonEmptyClientState(Client(clientId, Set.empty, "hello"))
    }

    "create model" in withTestDriver { driver =>
      val outcome1 = driver.run(CreateClient(clientId, "hello"))
      val outcome = driver.run(CreateModel(modelId, "nice model"))
      outcome.replies === Vector(ApiModelCreated(modelId))
      outcome.events should contain(ModelCreated(modelId, "nice model"))
      outcome.state === NonEmptyClientState(Client(clientId, Set(ModelDescription(modelId, "nice model")), "hello"))
    }

    "remove model" in withTestDriver { driver =>
      val outcome1 = driver.run(CreateClient(clientId, "hello"))
      val outcome2 = driver.run(CreateModel(modelId, "nice model"))

      val outcome = driver.run(RemoveModel(modelId))
      outcome.replies === Vector(Done)
      outcome.events should contain(ModelRemoved(modelId))
      outcome.state === NonEmptyClientState(Client(clientId, Set.empty, "hello"))
    }

    "remove model twice does not complain" in withTestDriver { driver =>
      val outcome1 = driver.run(CreateClient(clientId, "hello"))
      val outcome2 = driver.run(CreateModel(modelId, "nice model"))

      val outcome3 = driver.run(RemoveModel(modelId))
      val outcome = driver.run(RemoveModel(modelId))
      outcome.replies === Vector(Done)
      outcome.events should contain(ModelRemoved(modelId))
      outcome.state === NonEmptyClientState(Client(clientId, Set.empty, "hello"))
    }



  }
}
