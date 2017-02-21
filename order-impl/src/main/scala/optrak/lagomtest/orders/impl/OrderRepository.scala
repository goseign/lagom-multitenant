package optrak.lagomtest.orders.impl

import java.lang.Boolean

import akka.Done
import com.datastax.driver.core._
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import grizzled.slf4j.Logging
import optrak.lagomtest.datamodel.Models._
import optrak.lagomtest.orders.api.OrderIds
import optrak.lagomtest.orders.impl.OrderEvents.{OrderCreated, OrderEvent}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  * objective of this repository is to hold list of all orders by
  *
  * We follow the example in online-auction item repository, separating out the actual getting of the data
  * from read processor listening to the add order messages
  */
class OrderRepository(session: CassandraSession)(implicit ec: ExecutionContext) extends Logging {

  def selectOrdersForTenant(tenantId: TenantId): Future[OrderIds] = {
    val queryRes = session.selectAll(
      s"""
        | select orderId from orders where tenantId = '$tenantId'
      """.
        stripMargin)
      queryRes.map(rows =>
        OrderIds(
          rows.map { r =>
          val orderId = r.getString("orderId")
          orderId
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
private class OrderEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit executionContext: ExecutionContext)
extends ReadSideProcessor[OrderEvent] with Logging {

  private var insertOrderStatement : PreparedStatement = null

  // todo voodoo  - figure out what exactly is happening
  def aggregateTags = OrderEvent.Tag.allTags


  private def createTables() = {
    for {
      res <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS orders (
          tenantId text,
          orderId text,
          PRIMARY KEY (tenantId, orderId)
        )
      """)
    } yield {
      logger.debug(s"createTable result is $res")
      Done
    }
  }

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[OrderEvent] = {
    logger.debug(s"in buildHandler")
    readSide.builder[OrderEvent]("orderRepositoryOffset")
    .setGlobalPrepare(createTables)
    .setPrepare(_ => prepareStatements())
      .setEventHandler[OrderCreated](e => insertOrder(e.event.tenantId, e.event.orderId))
    .build
  }


  private def prepareStatements() = {
    // nb original (item repository) worked like this. The assignment to all vars executed only if all
    // prepared statements creation succeeded.
    // For a single prepared statement it's not required but we keep the pattern
    logger.debug(s"preparing statuements")
    for {
      insertOrder <- session.prepare(
        """
          | INSERT INTO orders(tenantId, orderId) VALUES (?, ?)
        """.stripMargin)
    } yield {
      insertOrderStatement = insertOrder
      logger.debug(s"ordered insert statment $insertOrder and $insertOrderStatement")
      Done
    }
  }

  // again copy pattern from item repository
  private def insertOrder(tenantId: TenantId, orderId: OrderId) = {
    logger.debug(s"insertOrder $tenantId $orderId")
    Future.successful(List(insertOrderStatement.bind(tenantId, orderId)))
  }


}


