package optrak.lagomtest.data

import optrak.lagomtest.data.Data._
import play.api.libs.json._

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object DataJson {
  implicit val formatProduct: Format[Product] = Json.format[Product]
  implicit val formatSite: Format[Site] = Json.format[Site]
  implicit val formatOrder: Format[Order] = Json.format[Order]

  implicit val formatVehicle: Format[Vehicle] = Json.format[Vehicle]
  implicit val formatTrip: Format[Trip] = Json.format[Trip]

  //implicit val formatSites: Format[Map[SiteId, Site]] = Json.format[Map[SiteId, Site]]
  //implicit val formatPlan: Format[Plan] = Json.format[Plan]
  implicit val formatPlanDescipriotn: Format[PlanDescription] = Json.format[PlanDescription]

  implicit val formatTenant: Format[Tenant] = Json.format[Tenant]

}
