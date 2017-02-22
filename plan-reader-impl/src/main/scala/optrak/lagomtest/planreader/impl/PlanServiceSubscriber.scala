package optrak.lagomtest.planreader.impl

/**
  * Created by tim on 30/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
import java.util.UUID

import akka.stream.scaladsl.Flow
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import optrak.lagomtest.plan.api.PlanEvents.PlanEvent
import optrak.lagomtest.plan.api.PlanService

class PlanServiceSubscriber(persistentEntityRegistry: PersistentEntityRegistry, planService: PlanService) {

  planService.planEvents.subscribe.atLeastOnce(Flow[PlanEvent].mapAsync(1) {
    case me: PlanEvent =>
      val planId = me.planId
      entityRef(planId).ask(WrappedPlanEvent(me))
  })


  private def entityRef(itemId: UUID) = persistentEntityRegistry.refFor[PlanReaderEntity](itemId.toString)

}
