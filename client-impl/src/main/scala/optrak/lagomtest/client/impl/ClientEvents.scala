package optrak.lagomtest.client.impl

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import optrak.lagomtest.datamodel.Models.{Client, ClientId, Model, ModelId}
import play.api.libs.json.{Format, Json}
import optrak.lagomtest.datamodel.ModelsJson._
  /**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object ClientEvents {
  // nb the client event needs to be aggregateEvent because it is used by read processor and needs an aggregate tag
  sealed trait ClientEvent extends AggregateEvent[ClientEvent] {
      override def aggregateTag = ClientEvent.Tag
  }

  object ClientEvent {
    val NumShards = 4
    val Tag = AggregateEventTag.sharded[ClientEvent](NumShards)
  }

  case class ClientCreated(id: String, description: String) extends ClientEvent

  object ClientCreated {
    implicit val format: Format[ClientCreated] = Json.format[ClientCreated]
  }

  case class ModelCreated(id: ModelId, description: String) extends ClientEvent

  object ModelCreated {
    implicit val format: Format[ModelCreated] = Json.format[ModelCreated]
  }

  case class ModelRemoved(id: ModelId) extends ClientEvent

  object ModelRemoved {
    implicit val format: Format[ModelRemoved] = Json.format[ModelRemoved]
  }

}
