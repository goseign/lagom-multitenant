package optrak.lagomtest.tenant.impl

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import optrak.lagomtest.tenant.api
import optrak.lagomtest.tenant.impl.TenantEvents.{ModelCreated, ModelRemoved, TenantCreated}

import scala.collection.immutable.Seq
import optrak.scalautils.json.JsonImplicits._
import optrak.lagom.utils.PlayJson4s._
/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object TenantSerializerRegistry extends JsonSerializerRegistry {

  override def serializers: Seq[JsonSerializer[_]] = {
    val res = Seq(
      JsonSerializer[CreateTenant],
      JsonSerializer[CreateModel],
      JsonSerializer[TenantCreated],
      JsonSerializer[ModelCreated],
      JsonSerializer[api.TenantCreationData],
      JsonSerializer[api.ModelCreationData],
      JsonSerializer[api.ModelCreated],
      JsonSerializer[RemoveModel],
      JsonSerializer[ModelRemoved]

    )
    res
  }
}