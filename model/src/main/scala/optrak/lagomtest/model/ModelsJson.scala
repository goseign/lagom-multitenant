package optrak.lagomtest.model

import optrak.lagomtest.model.Models.{Client, Model, Product, Site}
import play.api.libs.json.{Format, Json}

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object ModelsJson {
  implicit val formatProduct: Format[Product] = Json.format[Product]
  implicit val formatSite: Format[Site] = Json.format[Site]
  implicit val formatModel: Format[Model] = Json.format[Model]
  implicit val formatClient: Format[Client] = Json.format[Client]

}
