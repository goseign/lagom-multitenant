package optrak.lagomtest.datamodel.impl

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import optrak.lagomtest.model.impl.ModelCommands._
import optrak.lagomtest.model.api.ModelEvents._
import play.api.libs.json.Format

import scala.collection.immutable.Seq

/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object ModelSerializerRegistry extends JsonSerializerRegistry {

  override def serializers: Seq[JsonSerializer[_]] = {
    val res = Seq(
      JsonSerializer[CreateModel],
      JsonSerializer[ModelCreated],
      JsonSerializer[ProductUpdated],
      JsonSerializer[ProductRemoved],
      JsonSerializer[SiteUpdated],
      JsonSerializer[SiteRemoved],

    JsonSerializer[AddOrUpdateProduct],
    JsonSerializer[UpdateProduct],
    JsonSerializer[AddProduct],
    JsonSerializer[RemoveProduct],

    JsonSerializer[AddOrUpdateSite],
    JsonSerializer[UpdateSite],
    JsonSerializer[AddSite],
    JsonSerializer[RemoveSite]
      
      
    )
    res
  }
}