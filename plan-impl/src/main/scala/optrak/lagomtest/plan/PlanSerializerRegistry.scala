package optrak.lagomtest.plan

import com.lightbend.lagom.scaladsl.playjson
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import optrak.lagomtest.plan.api.PlanEvents._
import optrak.lagomtest.plan.PlanCommands._
import optrak.lagomtest.plan.api.PlanImpl

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

      JsonSerializer[VehicleUpdated],
      JsonSerializer[VehicleRemoved],

      JsonSerializer[OrderUpdated],
      JsonSerializer[OrderRemoved],

      JsonSerializer[UpdateProduct],
      JsonSerializer[AddProduct],
      JsonSerializer[RemoveProduct],

      JsonSerializer[UpdateSite],
      JsonSerializer[AddSite],
      JsonSerializer[RemoveSite],
      
      JsonSerializer[UpdateVehicle],
      JsonSerializer[AddVehicle],
      JsonSerializer[RemoveVehicle],
      
      JsonSerializer[UpdateOrder],
      JsonSerializer[AddOrder],
      JsonSerializer[RemoveOrder],

      JsonSerializer[PlanImpl]
      
    )
    res
  }
}