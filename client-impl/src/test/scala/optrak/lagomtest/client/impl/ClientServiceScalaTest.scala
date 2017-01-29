package optrak.lagomtest.client.impl

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
import java.io.File

import akka.Done
import akka.cluster.Cluster
import com.lightbend.lagom.scaladsl.api.transport.TransportException
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.UnhandledCommandException
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import optrak.lagomtest.client.api.{ClientService, ModelCreated}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import optrak.lagomtest.client.api.{CreateClient => ApiCreateClient, CreateModel => ApiCreateModel, ModelCreated => ApiModelCreated, RemoveModel => ApiRemoveModel, _}

import scala.concurrent.Await
import scala.concurrent.duration._

class ClientServiceScalaTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new ClientApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[ClientService]

  override protected def afterAll() = server.stop()

  val clientId = "tim"


  "client service" should {

    "create client" in {
      for {
        answer <- client.createClient(clientId).invoke(ApiCreateClient("my client"))
      } yield {
        answer should ===(Done)
      }
    }

    "complain about 2nd attempt create client" in {
      val exp = recoverToExceptionIf[TransportException](
      for {
        answer <- client.createClient(clientId).invoke(ApiCreateClient("my client"))
        answer2 <- client.createClient(clientId).invoke(ApiCreateClient("my client"))
      } yield {
        answer2 should ===(Done)
      })
      exp.map { te =>
        // println(s"te is code ${te.errorCode} message ${te.exceptionMessage}")
        te.toString should include("tim already exists")
      }
    }

    "removed model" in {
      for {
        added <- client.createModel(clientId).invoke(ApiCreateModel("nice model"))
        removed <- client.removeModel(clientId).invoke(ApiRemoveModel(added.id))
      } yield {
        added shouldBe a [ModelCreated]

        removed should === (Done)
      }
    }
  }
}
