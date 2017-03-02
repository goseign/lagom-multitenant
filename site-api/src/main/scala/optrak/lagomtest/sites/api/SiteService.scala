package optrak.lagomtest.sites.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.Service.pathCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import optrak.lagomtest.data.Data._
import optrak.scalautils.json.JsonImplicits._
import optrak.lagomtest.sites.api.SiteEvents.SiteEvent
import optrak.lagom.utils.PlayJson4s._
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  * the tenant for whom we are managing the sites
  * site id - nb externally defined so 2 tenants could have different sites with same id.
  *
  * Refer to datamodel for description of data model
  *
  * Note that for testing purposes only we have 2 different implementations of the site directory.
  *
  * One uses a readside processor that stores the data in Cassandra as tables and issues queries to return the information.
  * That database stores all site info in a single table across separate tenants, using the cql to get data for specific
  * tenant
  *
  * The other uses a readside processor in combination with a directory per tenant as an entity. I've no clear idea about
  * which is "better" or why for this use case but expect that factors will be how long it takes to reconstruct the entity,
  * what the update frequency is, how often requests are made and so on.
  */
trait SiteService extends Service {

  def createSite(tenant: TenantId, id: SiteId): ServiceCall[SiteCreationData, Done]

  def updatePostcode(tenant: TenantId, id: SiteId, newPostcode: String): ServiceCall[NotUsed, Done]

  def getSite(tenant: TenantId, id: SiteId): ServiceCall[NotUsed, Site]

  def checkSiteExists(tenant: TenantId, id: SiteId): ServiceCall[NotUsed, Done]

  def getSitesForTenant(tenant: TenantId): ServiceCall[NotUsed, SiteIds]


  override final def descriptor = {
    import Service._

    named("site").withCalls(
      pathCall("/optrak.lagom.sites.api/:tenant/postcode/:id/:newPostcode", updatePostcode _),
      pathCall("/optrak.lagom.sites.api/:tenant/create/:id", createSite _),
      pathCall("/optrak.lagom.sites.api/:tenant/site/:id", getSite _ ),
      pathCall("/optrak.lagom.sites.api/:tenant/sites", getSitesForTenant _ )
    )
      /*
    .withTopics(
      topic("site-directoryEvent", this.siteEvents)
      .addProperty(KafkaProperties.partitionKeyStrategy,
        PartitionKeyStrategy[SiteEvent](_.tenantId))
    ) */
    .withAutoAcl(true)
  }

  // def siteEvents: Topic[SiteEvent]


}

case class SiteCreationData(postcode: String)

case class SiteIds(ids: Set[SiteId])


