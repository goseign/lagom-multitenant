package optrak.lagomtest.sites.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import optrak.lagomtest.data.Data.{PlanId, SiteId, TenantId}
import play.api.libs.json.{Format, Json}
/**
* Created by tim on 22/01/17.
* Copyright Tim Pigden, Hertford UK
*/
object SiteEvents {

  // nb the siteevent needs to be aggregateEvent because it is used by read processor and needs an aggregate tag
  sealed trait SiteEvent extends AggregateEvent[SiteEvent] {
    override def aggregateTag = SiteEvent.Tag

    def tenantId: TenantId
    def siteId: SiteId
  }

  object SiteEvent {
    val NumShards = 4
    val Tag = AggregateEventTag.sharded[SiteEvent](NumShards)
  }

  case class SiteCreated(tenantId: TenantId, siteId: SiteId, postcode: String) extends SiteEvent
  case class SitePostcodeUpdated(tenantId: TenantId, siteId: SiteId, newPostcode: String) extends SiteEvent

  object SiteCreated {
    implicit def format: Format[SiteCreated] = Json.format[SiteCreated]
  }

  object SitePostcodeUpdated {
    implicit def format: Format[SitePostcodeUpdated] = Json.format[SitePostcodeUpdated]
  }

}
