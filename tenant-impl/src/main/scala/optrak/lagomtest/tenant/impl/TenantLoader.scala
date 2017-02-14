package optrak.lagomtest.tenant.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import optrak.lagomtest.tenant.api.TenantService
import play.api.Environment
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext

// separate out components relating to repository etc
trait TenantComponents extends LagomServerComponents
  with CassandraPersistenceComponents {

  implicit def executionContext: ExecutionContext

  // Bind the services that this server provides
  override lazy val lagomServer = LagomServer.forServices(
    bindService[TenantService].to(wire[TenantServiceImpl])
  )

  lazy val clientRepository = wire[TenantRepository]

  def environment: Environment


  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = TenantSerializerRegistry

  // Register the Tenant persistent entity
  persistentEntityRegistry.register(wire[TenantEntity])

  readSide.register(wire[TenantEventProcessor])

}

class TenantLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new TenantApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new TenantApplication(context) with LagomDevModeComponents
}

abstract class TenantApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
  with TenantComponents
    with AhcWSComponents {

}
