package optrak.lagomtest.plan

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import optrak.lagomtest.plan.api.PlanEvents._
import optrak.lagomtest.plan.PlanCommands._

import scala.collection.immutable.Seq

/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object PlanSerializerRegistry extends JsonSerializerRegistry {

  override def serializers: Seq[JsonSerializer[_]] = {
    val res = Seq(
      JsonSerializer[CreatePlan],
      JsonSerializer[PlanCreated],
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