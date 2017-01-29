package optrak.lagomtest.client.impl

import akka.{Done, NotUsed}
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import optrak.lagomtest.client.api
import optrak.lagomtest.client.api.{ClientService, ModelCreated}
import optrak.lagomtest.datamodel.Models
import optrak.lagomtest.datamodel.Models.ClientId
import optrak.lagomtest.client.api.{CreateClient => ApiCreateClient, CreateModel => ApiCreateModel, ModelCreated => ApiModelCreated, RemoveModel => ApiRemoveModel}

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ClientServiceImpl(persistentEntityRegistry: PersistentEntityRegistry, clientRepository: ClientRepository) extends ClientService {

  override def createClient(clientId: ClientId): ServiceCall[api.CreateClient, Done] = ServiceCall { request =>
    val ref = persistentEntityRegistry.refFor[ClientEntity](clientId)
    ref.ask(CreateClient(clientId, request.description))
  }

  override def createModel(clientId: ClientId): ServiceCall[api.CreateModel, ModelCreated] = ServiceCall { request =>
    val ref = persistentEntityRegistry.refFor[ClientEntity](clientId)
    val uuid = UUIDs.timeBased()
    ref.ask(CreateModel(uuid, request.description))
  }

  override def removeModel(clientId: ClientId): ServiceCall[api.RemoveModel, Done] = ServiceCall { request =>
    val ref = persistentEntityRegistry.refFor[ClientEntity](clientId)
    ref.ask(RemoveModel(request.id))
  }

  override def getClient(clientId: ClientId): ServiceCall[NotUsed, Models.Client] = ???

  override def getAllClients: ServiceCall[NotUsed, Seq[ClientId]] = ServiceCall { _ =>
    clientRepository.selectAllClients
  }


}

