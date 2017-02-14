package optrak.lagomtest.tenant.impl

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import optrak.lagomtest.datamodel.Models.ModelId
import play.api.libs.json.{Format, Json}
  /**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object TenantEvents {
  // nb the client event needs to be aggregateEvent because it is used by read processor and needs an aggregate tag
  sealed trait TenantEvent extends AggregateEvent[TenantEvent] {
      override def aggregateTag = TenantEvent.Tag
  }

  object TenantEvent {
    val NumShards = 4
    val Tag = AggregateEventTag.sharded[TenantEvent](NumShards)
  }

  case class TenantCreated(id: String, description: String) extends TenantEvent

  object TenantCreated {
    implicit val format: Format[TenantCreated] = Json.format[TenantCreated]
  }

  case class ModelCreated(id: ModelId, description: String) extends TenantEvent

  object ModelCreated {
    implicit val format: Format[ModelCreated] = Json.format[ModelCreated]
  }

  case class ModelRemoved(id: ModelId) extends TenantEvent

  object ModelRemoved {
    implicit val format: Format[ModelRemoved] = Json.format[ModelRemoved]
  }

}
