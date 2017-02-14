package optrak.lagomtest.modelreader.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import optrak.lagomtest.model.api.ModelService
import optrak.lagomtest.modelreader.api.ModelReaderService
import play.api.Environment
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext

// separate out components relating to repository etc
trait ModelReaderComponents extends LagomServerComponents
  with CassandraPersistenceComponents {

  implicit def executionContext: ExecutionContext

  // Bind the services that this server provides
  override lazy val lagomServer = LagomServer.forServices(
    bindService[ModelReaderService].to(wire[ModelReaderServiceImpl])
  )

  // lazy val ModelRepository = wire[ModelRepository]

  def environment: Environment


  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = ModelReaderSerializerRegistry

  // Register the Model persistent entity
  persistentEntityRegistry.register(wire[ModelReaderEntity])

//   readSide.register(wire[ModelEventProcessor])

}

class ModelReaderLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new ModelReaderApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new ModelReaderApplication(context) with LagomDevModeComponents
}

abstract class ModelReaderApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with ModelReaderComponents
    with AhcWSComponents
    with LagomKafkaComponents {
  lazy val modelService = serviceClient.implement[ModelService]
  wire[ModelServiceSubscriber]

}
