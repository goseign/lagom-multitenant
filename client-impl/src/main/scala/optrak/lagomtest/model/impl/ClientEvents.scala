package optrak.lagomtest.model.impl

import optrak.lagomtest.model.Models.{Client, Model, ModelId}
import play.api.libs.json.{Format, Json}
import optrak.lagomtest.model.ModelsJson._
  /**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object ClientEvents {
  sealed trait ClientEvent

  case class ClientChanged(client: Client) extends ClientEvent

  object ClientChanged {
    implicit val format: Format[ClientChanged] = Json.format[ClientChanged]
  }

  case class ClientAdded(client: Client) extends ClientEvent

  object ClientAdded {
    implicit val format: Format[ClientAdded] = Json.format[ClientAdded]
  }
    
  case class ModelAdded(model: Model) extends ClientEvent

  object ModelAdded {
    implicit val format: Format[ModelAdded] = Json.format[ModelAdded]
  }

  case class ModelUpdated(model: Model) extends ClientEvent

  object ModelUpdated {
    implicit val format: Format[ModelUpdated] = Json.format[ModelUpdated]
  }

  case class ModelRemved(id: ModelId) extends ClientEvent

  object ModelRemved {
    implicit val format: Format[ModelRemved] = Json.format[ModelRemved]
  }

}
