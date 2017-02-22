package optrak.lagomtest.planreader.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import optrak.lagomtest.plan.api.PlanService
import optrak.lagomtest.planreader.api.PlanReaderService
import play.api.Environment
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext

// separate out components relating to repository etc
trait PlanReaderComponents extends LagomServerComponents
  with CassandraPersistenceComponents {

  implicit def executionContext: ExecutionContext

  // Bind the services that this server provides
  override lazy val lagomServer = LagomServer.forServices(
    bindService[PlanReaderService].to(wire[PlanReaderServiceImpl])
  )

  // lazy val PlanRepository = wire[PlanRepository]

  def environment: Environment


  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = PlanReaderSerializerRegistry

  // Register the Plan persistent entity
  persistentEntityRegistry.register(wire[PlanReaderEntity])

//   readSide.register(wire[PlanEventProcessor])

}

class PlanReaderLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new PlanReaderApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new PlanReaderApplication(context) with LagomDevModeComponents
}

abstract class PlanReaderApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with PlanReaderComponents
    with AhcWSComponents
    with LagomKafkaComponents {
  lazy val planService = serviceClient.implement[PlanService]
  wire[PlanServiceSubscriber]

}
