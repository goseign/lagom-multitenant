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
class TenantServiceImpl(persistentEntityRegistry: PersistentEntityRegistry, tenantRepository: TenantRepository) extends TenantService {

  override def createTenant(tenantId: TenantId): ServiceCall[tenant.api.TenantCreationData, Done] = ServiceCall { request =>
    val ref = persistentEntityRegistry.refFor[TenantEntity](tenantId)
    ref.ask(CreateTenant(tenantId, request.description))
  }

  override def createModel(tenantId: TenantId): ServiceCall[tenant.api.ModelCreationData, ModelCreated] = ServiceCall { request =>
    val ref = persistentEntityRegistry.refFor[TenantEntity](tenantId)
    val uuid = UUIDs.timeBased()
    ref.ask(CreateModel(uuid, request.description))
  }

  override def removeModel(tenantId: TenantId, modelId: ModelId): ServiceCall[NotUsed, Done] = ServiceCall { request =>
    val ref = persistentEntityRegistry.refFor[TenantEntity](tenantId)
    ref.ask(RemoveModel(modelId))
  }

  override def getTenant(tenantId: TenantId): ServiceCall[NotUsed, Models.Tenant] = ???

  override def getAllTenants: ServiceCall[NotUsed, Seq[TenantId]] = ServiceCall { _ =>
    tenantRepository.selectAllTenants
  }


}

