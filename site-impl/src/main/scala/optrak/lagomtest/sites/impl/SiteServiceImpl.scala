package optrak.lagomtest.sites.impl

import akka.{Done, NotUsed}
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import grizzled.slf4j.Logging
import optrak.lagomtest.data.Data.{Site, SiteId, TenantId}
import optrak.lagomtest.sites.api.SiteEvents.{SiteCreated => ApiSiteCreated, SiteEvent => ApiSiteEvent}
import optrak.lagomtest.sites.api._
import optrak.lagomtest.sites.impl.SiteEvents.{SiteCreated, SiteEvent}

import scala.concurrent.ExecutionContext

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class SiteServiceImpl(persistentEntityRegistry: PersistentEntityRegistry,
                          siteRepository: SiteRepository
                        )
                        (implicit ec: ExecutionContext)
  extends SiteService with Logging {

  // note because we're runing a multi-tenancy app, the tenantId must be part of the entity id -
  // may have same site codes
  def entityId(tenantId: TenantId, site: SiteId) = s"$tenantId:$site"

  def ref(tenantId: TenantId, id: SiteId) =
      persistentEntityRegistry.refFor[SiteEntity](entityId(tenantId, id))


  override def createSite(tenantId: TenantId, id: SiteId): ServiceCall[SiteCreationData, Done] = ServiceCall { request =>
    logger.debug(s"creating site $id")
    ref(tenantId, id).ask(CreateSite(tenantId, id, request.postcode)).map { res =>
      logger.debug(s"created site $id")
      res
    }
  }
  override def updatePostcode(tenantId: TenantId, id: SiteId, newPostcode: String): ServiceCall[NotUsed, Done] = ServiceCall { request =>
    ref(tenantId, id).ask(UpdateSitePostcode(tenantId, id, newPostcode))
  }

  override def getSite(tenantId: TenantId, id: SiteId): ServiceCall[NotUsed, Site] = ServiceCall { request =>
    ref(tenantId, id).ask(GetSite).map {
      case Some(site) => site
      case None => throw NotFound(s"Site ${ref(tenantId, id)} not found")
    }
  }

  override def getSitesForTenant(tenantId: TenantId): ServiceCall[NotUsed, SiteIds] = ServiceCall { _ =>
    siteRepository.selectSitesForTenant(tenantId)
  }

}


