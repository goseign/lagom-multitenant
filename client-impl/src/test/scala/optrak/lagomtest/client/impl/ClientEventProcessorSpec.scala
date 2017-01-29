package optrak.lagomtest.client.impl

import java.time.{Duration, Instant}
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

import akka.persistence.query.Sequence
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server.LagomApplication
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import optrak.lagomtest.client.impl.ClientEvents.{ClientCreated, ClientEvent}
import optrak.lagomtest.datamodel.Models.{Client, ClientId}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.Future

class ClientEventProcessorSpec extends AsyncWordSpec with BeforeAndAfterAll with Matchers {

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
    new LagomApplication(ctx) with ClientComponents with AhcWSComponents {
      override def serviceLocator = NoServiceLocator
      override lazy val readSide: ReadSideTestDriver = new ReadSideTestDriver
    }
  }

  override def afterAll() = server.stop()

  private val testDriver = server.application.readSide
  private val clientRepository = server.application.clientRepository
  private val offset = new AtomicInteger()


  "The client event processor" should {
    "create a client" in {
      val clientCreated = ClientCreated("tim", "hello")
      for {
        _ <- feed(clientCreated.id, clientCreated)
        clients <- getClients
      } yield {
        clients should contain only clientCreated.id
      }
    }

  }

  private def getClients = {
    clientRepository.selectAllClients
  }

  private def feed(clientId: ClientId, event: ClientEvent) = {
    testDriver.feed(clientId.toString, event, Sequence(offset.getAndIncrement))
  }
}