package optrak.lagomtest.modelreader.impl

import java.util.UUID

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import optrak.lagomtest.datamodel.Models._
import optrak.lagomtest.datamodel.ModelsJson._
import optrak.lagomtest.modelreader.api.ModelReaderService
import com.lightbend.lagom.scaladsl.server.ServerServiceCall

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  * Microservice who's purpose is to build readable Models
  * Essentially these are a duplicate of the ModelEntity but designed for fast read access.
  * MessageBroker (kafka) is used to receive the update ModelEvent stream
  */
class ModelReaderServiceImpl(registry: PersistentEntityRegistry) extends ModelReaderService {

  override def getModel(id: UUID): ServiceCall[NotUsed, Model] = ServerServiceCall { _ =>
    entityRef(id).ask(GetModel)
  }

  private def entityRef(itemId: UUID) = entityRefString(itemId.toString)
  private def entityRefString(modelId: String) = registry.refFor[ModelReaderEntity](modelId)


}