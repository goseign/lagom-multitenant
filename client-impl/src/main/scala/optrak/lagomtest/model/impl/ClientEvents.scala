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

  case class ClientCreated(id: ClientId, description: String) extends ClientEvent

  object Created {
    implicit val format: Format[ClientCreated] = Json.format[ClientCreated]
  }

  case class ClientAdded(client: Client) extends ClientEvent

  object ClientAdded {
    implicit val format: Format[ClientAdded] = Json.format[ClientAdded]
  }
    
  case class ModelCreated(id: String, description: String) extends ClientEvent

  object ModelCreated {
    implicit val format: Format[ModelCreated] = Json.format[ModelCreated]
  }

  case class ModelUpdated(model: Model) extends ClientEvent

  object ModelUpdated {
    implicit val format: Format[ModelUpdated] = Json.format[ModelUpdated]
  }

  case class ModelRemoved(id: ModelId) extends ClientEvent

  object ModelRemoved {
    implicit val format: Format[ModelRemoved] = Json.format[ModelRemoved]
  }

}
