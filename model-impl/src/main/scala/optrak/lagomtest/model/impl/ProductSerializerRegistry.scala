package optrak.lagomtest.datamodel.impl

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.Format

import scala.collection.immutable.Seq

/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ProductSerializerRegistry extends JsonSerializerRegistry {

  implicitly[Format[Product]]

  override def serializers: Seq[JsonSerializer[_]] = {
    val res = Seq(
      JsonSerializer[SetProduct],
      JsonSerializer[Product],
      JsonSerializer[WithProduct],
      JsonSerializer[ProductChanged],
      JsonSerializer[EmptyProduct.type],
      JsonSerializer[GetProduct.type]
    )
    res
  }
}