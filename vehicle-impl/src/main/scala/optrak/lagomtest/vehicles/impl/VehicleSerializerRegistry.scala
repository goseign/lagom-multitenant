package optrak.lagomtest.vehicles.impl

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import optrak.lagomtest.data.Data.Vehicle
import optrak.scalautils.json.JsonImplicits._
import optrak.lagom.utils.PlayJson4s._
import optrak.lagomtest.vehicles.api.VehicleIds
import optrak.lagomtest.vehicles.impl.VehicleEvents._

import scala.collection.immutable.Seq

/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object VehicleSerializerRegistry extends JsonSerializerRegistry {

  override def serializers: Seq[JsonSerializer[_]] = {
    val res = Seq(
      JsonSerializer[CreateVehicle],
      JsonSerializer[UpdateVehicleCapacity],
      JsonSerializer[GetVehicle.type],
      JsonSerializer[Vehicle],
      JsonSerializer[VehicleCreated],
      JsonSerializer[VehicleCapacityUpdated],
      JsonSerializer[VehicleIds]

    )
    res
  }
}