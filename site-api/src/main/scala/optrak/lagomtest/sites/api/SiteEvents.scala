package optrak.lagomtest.sites.api

import com.lightbend.lagom.scaladsl.api.broker.Topic
import play.api.libs.json._
import optrak.lagomtest.data.Data._
import optrak.lagomtest.data.DataJson._

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

  object SiteCreated {
    implicit def format: Format[SiteCreated] = Json.format[SiteCreated]
  }

  implicit val reads: Reads[SiteEvent] = {
    (__ \ "event_type").read[String].flatMap {
      case "siteCreated" => implicitly[Reads[SiteCreated]].map(identity)
    }
  }
  implicit val writes: Writes[SiteEvent] = Writes { event =>
    val (jsValue, eventType) = event match {
      case m: SiteCreated => (Json.toJson(m)(SiteCreated.format), "siteCreated")
    }
    jsValue.transform(__.json.update((__ \ 'event_type).json.put(JsString(eventType)))).get
  }
}

