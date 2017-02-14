package optrak.lagomtest.modelreader.impl

/**
  * Created by tim on 30/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
import java.util.UUID

import akka.Done
import akka.stream.scaladsl.Flow
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import optrak.lagomtest.model.api.ModelEvents.ModelEvent
import optrak.lagomtest.model.api.ModelService

import scala.concurrent.Future

class ModelServiceSubscriber(persistentEntityRegistry: PersistentEntityRegistry, modelService: ModelService) {

  modelService.modelEvents.subscribe.atLeastOnce(Flow[ModelEvent].mapAsync(1) {
    case me: ModelEvent =>
      val modelId = me.modelId
      entityRef(modelId).ask(WrappedModelEvent(me))
  })


  private def entityRef(itemId: UUID) = persistentEntityRegistry.refFor[ModelReaderEntity](itemId.toString)

}
