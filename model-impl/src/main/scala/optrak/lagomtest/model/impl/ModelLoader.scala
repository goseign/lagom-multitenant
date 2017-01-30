package optrak.lagomtest.model.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import optrak.lagomtest.datamodel.impl.{ModelEntity, ModelSerializerRegistry, ModelServiceImpl}
import optrak.lagomtest.model.api.ModelService
import play.api.Environment
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext

// separate out components relating to repository etc
trait ModelComponents extends LagomServerComponents
  with CassandraPersistenceComponents {

  implicit def executionContext: ExecutionContext

  // Bind the services that this server provides
  override lazy val lagomServer = LagomServer.forServices(
    bindService[ModelService].to(wire[ModelServiceImpl])
  )

  // lazy val ModelRepository = wire[ModelRepository]

  def environment: Environment


  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = ModelSerializerRegistry

  // Register the Model persistent entity
  persistentEntityRegistry.register(wire[ModelEntity])

//   readSide.register(wire[ModelEventProcessor])

}

class ModelLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new ModelApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new ModelApplication(context) with LagomDevModeComponents
}

abstract class ModelApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
  with ModelComponents
    with AhcWSComponents {

}
