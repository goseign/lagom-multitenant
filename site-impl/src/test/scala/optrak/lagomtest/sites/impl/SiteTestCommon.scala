package optrak.lagomtest.sites.impl

import optrak.lagomtest.datamodel.Models.{Site, SiteId, TenantId}


/**
  * Created by tim on 14/02/17.
  * Copyright Tim Pigden, Hertford UK
  */
object SiteTestCommon {

  val tenantId = "tenant1"
  val tenant2 = "tenant2"
  val site1Id = "site1"
  val site1Size = 10
  val site2Id = "site2"
  val site2Size = 2
  val postcode1 = "postcode1"
  val postcode2 = "postcode2"
  val site1 = Site(site1Id, postcode1)
  val site2 = Site(site2Id, postcode2)

  val site1g2 = site1.copy(postcode = postcode2)

  def entityId(tenantId: TenantId, siteId: SiteId) = s"$tenantId:$siteId"

  val createSite1 = CreateSite(tenantId, site1Id, postcode1)

}
