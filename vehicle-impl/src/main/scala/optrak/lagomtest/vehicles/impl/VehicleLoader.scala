package optrak.lagomtest.vehicles.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import optrak.lagomtest.vehicles.api.VehicleService
import play.api.Environment
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext

// separate out components relating to repository etc
trait VehicleComponents extends LagomServerComponents
  with CassandraPersistenceComponents {

  implicit def executionContext: ExecutionContext

  // Bind the services that this server provides
  override lazy val lagomServer = LagomServer.forServices(
    bindService[VehicleService].to(wire[VehicleServiceImpl])
  )

  lazy val vehicleRepository = wire[VehicleRepository]

  def environment: Environment


  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = VehicleSerializerRegistry

  // Register the Vehicle persistent entity
  persistentEntityRegistry.register(wire[VehicleEntity])

  readSide.register(wire[VehicleEventDbProcessor])

}

class VehicleLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new VehicleApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new VehicleApplication(context) with LagomDevModeComponents
}

abstract class VehicleApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
  with VehicleComponents
    with AhcWSComponents
    with LagomKafkaComponents {

}
