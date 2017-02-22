package optrak.lagomtest.vehicles.api

import optrak.lagomtest.data.Data._
import play.api.libs.json._

/**
* Created by tim on 22/01/17.
* Copyright Tim Pigden, Hertford UK
  * Note this is different from  oiptrak.lagomtest.vehicles.impl.VehicleEvents in the way the events are defined
*/
object VehicleEvents {

  sealed trait VehicleEvent {
    def tenantId: TenantId

    def vehicleId: VehicleId
  }

  case class VehicleCreated(tenantId: TenantId, vehicleId: VehicleId) extends VehicleEvent

  object VehicleCreated {
    implicit def format: Format[VehicleCreated] = Json.format[VehicleCreated]
  }

  implicit val reads: Reads[VehicleEvent] = {
    (__ \ "event_type").read[String].flatMap {
      case "vehicleCreated" => implicitly[Reads[VehicleCreated]].map(identity)
    }
  }
  implicit val writes: Writes[VehicleEvent] = Writes { event =>
    val (jsValue, eventType) = event match {
      case m: VehicleCreated => (Json.toJson(m)(VehicleCreated.format), "vehicleCreated")
    }
    jsValue.transform(__.json.update((__ \ 'event_type).json.put(JsString(eventType)))).get
  }
}

