package optrak.lagom.products.impl

import com.lightbend.lagom.scaladsl.playjson.Jsonable
import optrak.lagom.products.api.Product
import play.api.libs.json.{Format, Json}
  /**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object ProductEvents {
  sealed trait ProductEvent extends Jsonable

  case class ProductChanged(product: Product) extends ProductEvent

  object ProductChanged {
    implicit val format: Format[ProductChanged] = Json.format[ProductChanged]
  }

}
