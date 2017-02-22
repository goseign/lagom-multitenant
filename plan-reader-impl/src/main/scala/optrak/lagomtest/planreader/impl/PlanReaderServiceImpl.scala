package optrak.lagomtest.planreader.impl

import java.util.UUID

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import optrak.lagomtest.data.Data._
import optrak.lagomtest.plan.api.PlanService.SimplePlan
import optrak.lagomtest.planreader.api.PlanReaderService

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  * Microservice who's purpose is to build readable Plans
  * Essentially these are a duplicate of the PlanEntity but designed for fast read access.
  * MessageBroker (kafka) is used to receive the update PlanEvent stream
  */
class PlanReaderServiceImpl(registry: PersistentEntityRegistry) extends PlanReaderService {

  override def getPlan(id: UUID): ServiceCall[NotUsed, SimplePlan] = ServerServiceCall { _ =>
    entityRef(id).ask(GetPlan)
  }

  private def entityRef(itemId: UUID) = entityRefString(itemId.toString)
  private def entityRefString(planId: String) = registry.refFor[PlanReaderEntity](planId)


}