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

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class ClientServiceRepositoryTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new ClientApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[ClientService]

  override protected def afterAll() = server.stop()

  val clientId = "tim"

  def doCreate(clientId: String): Future[Done] = {
    val fDone: Future[Done] = client.createClient(clientId).invoke(ApiCreateClient("my client"))
    fDone
  }


  "client service" should {

    "create client" in {

      doCreate("tim")
      doCreate("tom")
      Thread.sleep(20000)
      for {
        recoveredClients <- client.getAllClients.invoke()
      } yield { recoveredClients.toSet should === (Set("tim", "tom")) }
    }

    "create many" in {
      val iSet = 1.to(200).map {i => i.toString }
      iSet.foreach { i => doCreate(i) }
      Thread.sleep(20000)
      for {
        recoveredClients <- client.getAllClients.invoke()
      } yield { recoveredClients.toSet should === (Set("tim", "tom") ++ iSet) }

    }
  }
}
