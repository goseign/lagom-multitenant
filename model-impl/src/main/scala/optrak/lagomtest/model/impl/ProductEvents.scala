package optrak.lagomtest.model.impl

import optrak.lagomtest.model.api.Product
import play.api.libs.json.{Format, Json}
  /**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object ProductEvents {
  sealed trait ProductEvent

  case class ProductChanged(product: Product) extends ProductEvent

  object ProductChanged {
    implicit val format: Format[ProductChanged] = Json.format[ProductChanged]
  }

}
