package optrak.lagomtest.orders.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import optrak.lagomtest.orders.api.OrderService
import play.api.Environment
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext

// separate out components relating to repository etc
trait OrderComponents extends LagomServerComponents
  with CassandraPersistenceComponents {

  implicit def executionContext: ExecutionContext

  // Bind the services that this server provides
  override lazy val lagomServer = LagomServer.forServices(
    bindService[OrderService].to(wire[OrderServiceImpl])
  )

  // lazy val orderRepository = wire[OrderRepository]

  def environment: Environment


  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = OrderSerializerRegistry

  // Register the Order persistent entity
  persistentEntityRegistry.register(wire[OrderEntity])
  persistentEntityRegistry.register(wire[TenantOrderDirectoryEntity])

  // wire[OrderEventSubscriber]

  // readSide.register(wire[OrderEventProcessor])

}

class OrderLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new OrderApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new OrderApplication(context) with LagomDevModeComponents
}

abstract class OrderApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
  with OrderComponents
    with AhcWSComponents
    with LagomKafkaComponents {

}
