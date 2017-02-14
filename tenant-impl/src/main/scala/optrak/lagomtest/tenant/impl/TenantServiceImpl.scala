package optrak.lagomtest.tenant.impl

import akka.{Done, NotUsed}
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import optrak.lagomtest.datamodel.Models
import optrak.lagomtest.datamodel.Models.{ModelId, TenantId}
import optrak.lagomtest.tenant
import optrak.lagomtest.tenant.api.{ModelCreated, TenantService}

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class TenantServiceImpl(persistentEntityRegistry: PersistentEntityRegistry, clientRepository: TenantRepository) extends TenantService {

  override def createTenant(clientId: TenantId): ServiceCall[tenant.api.TenantCreationData, Done] = ServiceCall { request =>
    val ref = persistentEntityRegistry.refFor[TenantEntity](clientId)
    ref.ask(CreateTenant(clientId, request.description))
  }

  override def createModel(clientId: TenantId): ServiceCall[tenant.api.ModelCreationData, ModelCreated] = ServiceCall { request =>
    val ref = persistentEntityRegistry.refFor[TenantEntity](clientId)
    val uuid = UUIDs.timeBased()
    ref.ask(CreateModel(uuid, request.description))
  }

  override def removeModel(clientId: TenantId, modelId: ModelId): ServiceCall[NotUsed, Done] = ServiceCall { request =>
    val ref = persistentEntityRegistry.refFor[TenantEntity](clientId)
    ref.ask(RemoveModel(modelId))
  }

  override def getTenant(clientId: TenantId): ServiceCall[NotUsed, Models.Tenant] = ???

  override def getAllTenants: ServiceCall[NotUsed, Seq[TenantId]] = ServiceCall { _ =>
    clientRepository.selectAllTenants
  }


}

