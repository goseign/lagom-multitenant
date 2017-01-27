package optrak.lagomtest.model.impl

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import optrak.lagomtest.model.Models
import optrak.lagomtest.model.Models.{ClientId, ModelId, ProductId, SiteId}
import optrak.lagomtest.model.api.{CreateClient, CreateModel, ModelCreated, ModelService}

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ClientServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends ModelService {
  override def createClient(clientId: ClientId): ServiceCall[CreateClient, Done] = ???

  override def createModel(clientId: ClientId): ServiceCall[CreateModel, ModelCreated] = ???

  override def removeModel(modelId: ModelId): Unit = ???
}
