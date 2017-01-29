package optrak.lagomtest.client.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import optrak.lagomtest.client.api.ClientService
import play.api.Environment
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext

// separate out components relating to repository etc
trait ClientComponents extends LagomServerComponents
  with CassandraPersistenceComponents {

  implicit def executionContext: ExecutionContext

  // Bind the services that this server provides
  override lazy val lagomServer = LagomServer.forServices(
    bindService[ClientService].to(wire[ClientServiceImpl])
  )

  lazy val clientRepository = wire[ClientRepository]

  def environment: Environment


  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = ClientSerializerRegistry

  // Register the Client persistent entity
  persistentEntityRegistry.register(wire[ClientEntity])

  readSide.register(wire[ClientEventProcessor])

}

class ClientLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new ClientApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new ClientApplication(context) with LagomDevModeComponents
}

abstract class ClientApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
  with ClientComponents
    with AhcWSComponents {

}
