package optrak.lagomtest.sites.api

import com.lightbend.lagom.scaladsl.api.broker.Topic
import play.api.libs.json._
import optrak.lagomtest.data.Data._

/**
* Created by tim on 22/01/17.
* Copyright Tim Pigden, Hertford UK
  * Note this is different from  oiptrak.lagomtest.sites.impl.SiteEvents in the way the events are defined
*/
object SiteEvents {

  sealed trait SiteEvent {
    def tenantId: TenantId

    def siteId: SiteId
  }

  case class SiteCreated(tenantId: TenantId, siteId: SiteId) extends SiteEvent

}

