package optrak.lagomtest.planreader.api

import java.util.UUID

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import optrak.lagomtest.data.Data._
import optrak.lagomtest.data.DataJson._
import optrak.lagomtest.plan.api.PlanService.SimplePlan
import play.api.libs.json._
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  * Microservice who's purpose is to build readable Plans
  * Essentially these are a duplicate of the PlanEntity but designed for fast read access.
  * MessageBroker (kafka) is used to receive the update PlanEvent stream
  */
trait PlanReaderService extends Service {


  def getPlan(id: UUID): ServiceCall[NotUsed, SimplePlan]


  final override def descriptor = {
    import Service._

    named("planreader").withCalls(
      pathCall("/api/plan:id", getPlan _)
    )
  }



}