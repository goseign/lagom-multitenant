package optrak.lagomtest.products.impl

import java.lang.Boolean

import akka.Done
import com.datastax.driver.core._
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import optrak.lagomtest.datamodel.Models._
import optrak.lagomtest.products.impl.ProductEvents.{ProductCancelled, ProductCreated, ProductEvent}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  * objective of this repository is to hold list of all created clients
  *
  * We follow the example in online-auction item repository, separating out the actual getting of the data
  * from read processor listening to the add client messages
  */
class ProductRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  def selectAllProducts: Future[Seq[ProductId]] = {
    session.selectAll(
      """
        | select * from clients
      """.stripMargin).map( rows => rows.map(_.getString("clientId")))
  }


}

/**
  * This follows closely the model from ItemEventProcessor in online auction
  * @param session
  * @param readSide
  * @param executionContext
  */
private class ProductEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit executionContext: ExecutionContext)
extends ReadSideProcessor[ProductEvent] {

  private var insertProductStatement : PreparedStatement = null
  private var cancelProductStatement : PreparedStatement = null

  // todo voodoo  - figure out what exactly is happening
  def aggregateTags = ProductEvent.Tag.allTags


  private def createTables() = {
    for {
      _ <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS products (
          clientId text,
          productId text,
          cancelled boolean,
          PRIMARY KEY (clientId, productId)
        )
      """)
    } yield Done
  }

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[ProductEvent] = {
    readSide.builder[ProductEvent]("clientRepositoryOffset")
    .setGlobalPrepare(createTables)
    .setPrepare(_ => prepareStatements())
      .setEventHandler[ProductCreated](e => insertProduct(e.event.clientId, e.event.id, false))
      .setEventHandler[ProductCancelled](e => cancelProduct(e.event.clientId, e.event.id))
    .build
  }


  private def prepareStatements() = {
    // nb original (item repository) worked like this. The assignment to all vars executed only if all
    // prepared statements creation succeeded.
    // For a single prepared statement it's not required but we keep the pattern
    for {
      insertProduct <- session.prepare(
        """
          | Insert into products(clientId, productId, cancelled) values (?, ?, ?)
        """.stripMargin)
      cancelProduct <- session.prepare(
        """
          | Update products
          |  set cancelled = true
          |  where clientId = ? and productId = ?
        """.stripMargin)
    } yield {
      insertProductStatement = insertProduct
      cancelProductStatement = cancelProduct
      Done
    }
  }

  // again copy pattern from item repository
  private def insertProduct(clientId: TenantId, productId: ProductId, cancelled: Boolean) = {
    println(s"insertProduct $clientId")
    Future.successful(List(insertProductStatement.bind(clientId, productId, new java.lang.Boolean(cancelled))))
  }

  // again copy pattern from item repository
  private def cancelProduct(clientId: TenantId, productId: ProductId) = {
    println(s"insertProduct $clientId")
    Future.successful(List(cancelProductStatement.bind(clientId, productId, java.lang.Boolean.TRUE)))
  }

}


