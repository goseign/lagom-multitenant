package optrak.lagomtest.model.impl

import akka.{Done, NotUsed}
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import optrak.lagomtest.model.Models
import optrak.lagomtest.model.Models.{ClientId, ModelId, ProductId, SiteId}
import optrak.lagomtest.model.api.ClientService
import optrak.lagomtest.model._
import optrak.lagomtest.model.api.{CreateClient => ApiCreateClient, CreateModel => ApiCreateModel, ModelCreated => ApiModelCreated, RemoveModel => ApiRemoveModel}

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ClientServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends ClientService {

  override def createClient(clientId: ClientId): ServiceCall[ApiCreateClient, Done] = ServiceCall { request =>
    val ref = persistentEntityRegistry.refFor[ClientEntity](clientId)
    ref.ask(CreateClient(clientId, request.description))
  }

  override def createModel(clientId: ClientId): ServiceCall[ApiCreateModel, ApiModelCreated] = ServiceCall { request =>
    val ref = persistentEntityRegistry.refFor[ClientEntity](clientId)
    val uuid = UUIDs.timeBased()
    ref.ask(CreateModel(uuid, request.description))
  }

  override def removeModel(clientId: ClientId): ServiceCall[ApiRemoveModel, Done] = ServiceCall { request =>
    val ref = persistentEntityRegistry.refFor[ClientEntity](clientId)
    ref.ask(RemoveModel(request.id))
  }

  override def getClient(clientId: ClientId): ServiceCall[NotUsed, Models.Client] = ???
  override def getAllClients: ServiceCall[NotUsed, Seq[Models.Client]] = ???


}

