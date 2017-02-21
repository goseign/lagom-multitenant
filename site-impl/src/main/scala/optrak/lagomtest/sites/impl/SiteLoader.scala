package optrak.lagomtest.sites.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import optrak.lagomtest.sites.api.SiteService
import play.api.Environment
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext

// separate out components relating to repository etc
trait SiteComponents extends LagomServerComponents
  with CassandraPersistenceComponents {

  implicit def executionContext: ExecutionContext

  // Bind the services that this server provides
  override lazy val lagomServer = LagomServer.forServices(
    bindService[SiteService].to(wire[SiteServiceImpl])
  )

  lazy val siteRepository = wire[SiteRepository]

  def environment: Environment


  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = SiteSerializerRegistry

  // Register the Site persistent entity
  persistentEntityRegistry.register(wire[SiteEntity])

  readSide.register(wire[SiteEventDbProcessor])

}

class SiteLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new SiteApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new SiteApplication(context) with LagomDevModeComponents
}

abstract class SiteApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
  with SiteComponents
    with AhcWSComponents
    with LagomKafkaComponents {

}
