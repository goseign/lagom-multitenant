package optrak.lagomtest.sites.impl

import java.lang.Boolean

import akka.Done
import com.datastax.driver.core._
import com.lightbend.lagom.scaladsl.persistence.{PersistentEntityRegistry, ReadSideProcessor}
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import grizzled.slf4j.Logging
import optrak.lagomtest.datamodel.Models._
import optrak.lagomtest.sites.api.SiteIds
import optrak.lagomtest.sites.impl.SiteEvents.{SiteCreated, SiteEvent}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  * objective of this repository is to hold list of all sites by
  *
  * We follow the example in online-auction item repository, separating out the actual getting of the data
  * from read processor listening to the add site messages
  */
class SiteRepository(session: CassandraSession)(implicit ec: ExecutionContext) extends Logging {

  def selectSitesForTenant(tenantId: TenantId): Future[SiteIds] = {
    val queryRes = session.selectAll(
      s"""
        | select siteId from sites where tenantId = '$tenantId'
      """.
        stripMargin)
      queryRes.map(rows =>
        SiteIds(rows.map { r =>
          val siteId = r.getString("siteId")
          siteId
        }.toSet)
      )
    }


}
/**
  * This follows closely the model from ItemEventProcessor in online auction
  * @param session
  * @param readSide
  * @param executionContext
  */
private class SiteEventDbProcessor(session: CassandraSession,
                                      readSide: CassandraReadSide)
                                     (implicit executionContext: ExecutionContext)
extends ReadSideProcessor[SiteEvent] with Logging {

  private var insertSiteStatement : PreparedStatement = null

  // todo voodoo  - figure out what exactly is happening
  def aggregateTags = SiteEvent.Tag.allTags


  private def createTables() = {
    for {
      res <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS sites (
          tenantId text,
          siteId text,
          PRIMARY KEY (tenantId, siteId)
        )
      """)
    } yield {
      logger.debug(s"createTable result is $res")
      Done
    }
  }

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[SiteEvent] = {
    logger.debug(s"in buildHandler")
    readSide.builder[SiteEvent]("siteRepositoryOffset")
    .setGlobalPrepare(createTables)
    .setPrepare(_ => prepareStatements())
      .setEventHandler[SiteCreated](e => insertSite(e.event.tenantId, e.event.siteId))
    .build
  }


  private def prepareStatements() = {
    // nb original (item repository) worked like this. The assignment to all vars executed only if all
    // prepared statements creation succeeded.
    // For a single prepared statement it's not required but we keep the pattern
    logger.debug(s"preparing statuements")
    for {
      insertSite <- session.prepare(
        """
          | INSERT INTO sites(tenantId, siteId) VALUES (?, ?)
        """.stripMargin)
    } yield {
      insertSiteStatement = insertSite
      logger.debug(s"siteed insert statment $insertSite and $insertSiteStatement")
      Done
    }
  }


  // note we're inserting site but we're also sending it to the TenantSiteDirectory - just because
  // we are testing both methods
  private def insertSite(tenantId: TenantId, siteId: SiteId) = {
    Future.successful(List(insertSiteStatement.bind(tenantId, siteId)))

  }


}
