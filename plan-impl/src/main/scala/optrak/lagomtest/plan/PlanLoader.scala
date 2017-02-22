package optrak.lagomtest.plan

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import optrak.lagomtest.plan.api.PlanService
import play.api.Environment
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext

// separate out components relating to repository etc
trait PlanComponents extends LagomServerComponents
  with CassandraPersistenceComponents {

  implicit def executionContext: ExecutionContext

  // Bind the services that this server provides
  override lazy val lagomServer = LagomServer.forServices(
    bindService[PlanService].to(wire[PlanServiceImpl])
  )

  // lazy val PlanRepository = wire[PlanRepository]

  def environment: Environment


  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = PlanSerializerRegistry

  // Register the Plan persistent entity
  persistentEntityRegistry.register(wire[PlanEntity])

//   readSide.register(wire[PlanEventProcessor])

}

class PlanLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new PlanApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new PlanApplication(context) with LagomDevModeComponents
}

abstract class PlanApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
  with PlanComponents
    with AhcWSComponents {

}
