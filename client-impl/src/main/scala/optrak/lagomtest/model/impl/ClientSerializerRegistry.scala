package optrak.lagomtest.model.impl

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import optrak.lagomtest.model.impl.ClientEvents.{ClientCreated, ModelCreated, ModelRemoved}
import play.api.libs.json.Format
import optrak.lagomtest.model.api.{ModelCreated => ApiModelCreated, CreateClient => ApiCreateClient, CreateModel => ApiCreateModel, RemoveModel => ApiRemoveModel}

import scala.collection.immutable.Seq

/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object ClientSerializerRegistry extends JsonSerializerRegistry {

  override def serializers: Seq[JsonSerializer[_]] = {
    val res = Seq(
      JsonSerializer[CreateClient],
      JsonSerializer[CreateModel],
      JsonSerializer[ClientCreated],
      JsonSerializer[ModelCreated],
      JsonSerializer[ApiCreateClient],
      JsonSerializer[ApiCreateModel],
      JsonSerializer[ApiModelCreated],
      JsonSerializer[ApiRemoveModel],
      JsonSerializer[RemoveModel],
      JsonSerializer[ModelRemoved],
      JsonSerializer[EmptyClientState.type],
      JsonSerializer[NonEmptyClientState]

    )
    res
  }
}