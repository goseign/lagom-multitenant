package optrak.lagomtest.tenant.impl

import akka.Done
import com.datastax.driver.core._
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import optrak.lagomtest.datamodel.Models.TenantId
import optrak.lagomtest.tenant.impl.TenantEvents.{TenantCreated, TenantEvent}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  * objective of this repository is to hold list of all created tenants
  *
  * We follow the example in online-auction item repository, separating out the actual getting of the data
  * from read processor listening to the add tenant messages
  */
class TenantRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  def selectAllTenants: Future[Seq[TenantId]] = {
    session.selectAll(
      """
        | select * from tenants
      """.stripMargin).map( rows => rows.map(_.getString("tenantId")))
  }


}

/**
  * This follows closely the model from ItemEventProcessor in online auction
  * @param session
  * @param readSide
  * @param executionContext
  */
private class TenantEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit executionContext: ExecutionContext)
extends ReadSideProcessor[TenantEvent] {

  private var insertTenantIdStatement : PreparedStatement = null

  // todo voodoo  - figure out what exactly is happening
  def aggregateTags = TenantEvent.Tag.allTags


  private def createTables() = {
    for {
      _ <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS tenants (
          tenantId text PRIMARY KEY
        )
      """)
    } yield Done
  }

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[TenantEvent] = {
    readSide.builder[TenantEvent]("tenantRepositoryOffset")
    .setGlobalPrepare(createTables)
    .setPrepare(_ => prepareStatements())
    .setEventHandler[TenantCreated](e => insertTenantId(e.event.id))
    .build
  }


  private def prepareStatements() = {
    // nb original (item repository) worked like this. The assignment to all vars executed only if all
    // prepared statements creation succeeded.
    // For a single prepared statement it's not required but we keep the pattern
    for {
      insertTenantId <- session.prepare(
        """
          | Insert into tenants(tenantId) values (?)
        """.stripMargin)
    } yield {
      insertTenantIdStatement = insertTenantId
      Done
    }
  }

  // again copy pattern from item repository
  private def insertTenantId(tenantId: TenantId) = {
    println(s"insertTenantId $tenantId")
    Future.successful(List(insertTenantIdStatement.bind(tenantId)))
  }


}


