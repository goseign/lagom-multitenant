package optrak.lagomtest.sites.impl

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import optrak.lagomtest.data.Data.{Site, SiteId}
import optrak.lagomtest.sites.impl.SiteEvents._
import optrak.lagomtest.data.DataJson._
import optrak.lagomtest.sites.api.SiteIds

import scala.collection.immutable.Seq

/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object SiteSerializerRegistry extends JsonSerializerRegistry {

  override def serializers: Seq[JsonSerializer[_]] = {
    val res = Seq(
      JsonSerializer[CreateSite],
      JsonSerializer[UpdateSitePostcode],
      JsonSerializer[GetSite.type],
      JsonSerializer[Site],
      JsonSerializer[SiteCreated],
      JsonSerializer[SitePostcodeUpdated],
      JsonSerializer[SiteIds]

    )
    res
  }
}