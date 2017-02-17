package optrak.lagomtest.products.impl

import java.lang.Boolean

import akka.Done
import com.datastax.driver.core._
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import grizzled.slf4j.Logging
import optrak.lagomtest.datamodel.Models._
import optrak.lagomtest.products.api.ProductStatus
import optrak.lagomtest.products.impl.ProductEvents.{ProductCancelled, ProductCreated, ProductEvent}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  * objective of this repository is to hold list of all products by
  *
  * We follow the example in online-auction item repository, separating out the actual getting of the data
  * from read processor listening to the add product messages
  */
class ProductRepository(session: CassandraSession)(implicit ec: ExecutionContext) extends Logging {

  def selectProductsForTenant(tenantId: TenantId): Future[Seq[ProductStatus]] = {
    val queryRes = session.selectAll(
      s"""
        | select productId, cancelled from products where tenantId = '$tenantId'
      """.
        stripMargin)
      queryRes.map(rows =>
        rows.map { r =>
          val productId = r.getString("productId")
          val cancelled = r.getBool("cancelled")
          ProductStatus(productId, cancelled)
        }
      )
    }

  def selectLiveProductsForTenant(tenantId: TenantId): Future[Seq[ProductId]] = {

    // cancelling is uncommon so we can allow filtering
    val queryRes = session.selectAll(
      s"""
         | select productId from products where tenantId = '$tenantId' and cancelled = FALSE allow filtering
      """.
        stripMargin)
    queryRes.map(rows =>
      rows.map { r =>
        r.getString("productId")
      }
    )
  }

}
/**
  * This follows closely the model from ItemEventProcessor in online auction
  * @param session
  * @param readSide
  * @param executionContext
  */
private class ProductEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit executionContext: ExecutionContext)
extends ReadSideProcessor[ProductEvent] with Logging {

  private var insertProductStatement : PreparedStatement = null
  private var cancelProductStatement : PreparedStatement = null

  // todo voodoo  - figure out what exactly is happening
  def aggregateTags = ProductEvent.Tag.allTags


  private def createTables() = {
    for {
      res <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS products (
          tenantId text,
          productId text,
          cancelled boolean,
          PRIMARY KEY (tenantId, productId)
        )
      """)
    } yield {
      logger.debug(s"createTable result is $res")
      Done
    }
  }

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[ProductEvent] = {
    logger.debug(s"in buildHandler")
    readSide.builder[ProductEvent]("productRepositoryOffset")
    .setGlobalPrepare(createTables)
    .setPrepare(_ => prepareStatements())
      .setEventHandler[ProductCreated](e => insertProduct(e.event.tenantId, e.event.id, false))
      .setEventHandler[ProductCancelled](e => cancelProduct(e.event.tenantId, e.event.id))
    .build
  }


  private def prepareStatements() = {
    // nb original (item repository) worked like this. The assignment to all vars executed only if all
    // prepared statements creation succeeded.
    // For a single prepared statement it's not required but we keep the pattern
    logger.debug(s"preparing statuements")
    for {
      insertProduct <- session.prepare(
        """
          | INSERT INTO products(tenantId, productId, cancelled) VALUES (?, ?, FALSE)
        """.stripMargin)
      cancelProduct <- session.prepare(
        """
          | Update products
          |  set cancelled = true
          |  where tenantId = ? and productId = ?
        """.stripMargin)
    } yield {
      insertProductStatement = insertProduct
      cancelProductStatement = cancelProduct
      logger.debug(s"producted insert statment $insertProduct and $insertProductStatement")
      Done
    }
  }

  // again copy pattern from item repository
  private def insertProduct(tenantId: TenantId, productId: ProductId, cancelled: Boolean) = {
    logger.debug(s"insertProduct $tenantId $productId $cancelled")
    Future.successful(List(insertProductStatement.bind(tenantId, productId /*, new java.lang.Boolean(cancelled)*/)))
  }

  // again copy pattern from item repository
  private def cancelProduct(tenantId: TenantId, productId: ProductId) = {
    logger.debug(s"cancelProduct $tenantId")
    Future.successful(List(cancelProductStatement.bind(tenantId, productId)))
  }

}


