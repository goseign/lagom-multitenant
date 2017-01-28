package optrak.lagomtest.model.impl

import optrak.lagomtest.model.Models.{Client, ClientId, Model, ModelId}
import play.api.libs.json.{Format, Json}
import optrak.lagomtest.model.ModelsJson._
  /**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object ClientEvents {
  sealed trait ClientEvent

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
