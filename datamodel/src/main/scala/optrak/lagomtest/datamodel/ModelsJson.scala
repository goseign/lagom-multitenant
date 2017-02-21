package optrak.lagomtest.datamodel

import optrak.lagomtest.datamodel.Models._
import play.api.libs.json._

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object ModelsJson {
  implicit val formatProduct: Format[Product] = Json.format[Product]
  implicit val formatSite: Format[Site] = Json.format[Site]
  implicit val formatOrderValue: Format[OrderValue] = Json.format[OrderValue]
  implicit val formatOrder: Format[Order] = Json.format[Order]

  //implicit val formatSites: Format[Map[SiteId, Site]] = Json.format[Map[SiteId, Site]]
  implicit val formatModel: Format[Model] = Json.format[Model]
  implicit val formatModelDescipriotn: Format[ModelDescription] = Json.format[ModelDescription]

  implicit val formatTenant: Format[Tenant] = Json.format[Tenant]

}
