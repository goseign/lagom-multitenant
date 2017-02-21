package optrak.lagomtest.orders.impl

import java.lang.Boolean

import akka.Done
import com.datastax.driver.core._
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import grizzled.slf4j.Logging
import optrak.lagomtest.datamodel.Models._
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

  def selectOrdersForTenant(tenantId: TenantId): Future[Seq[OrderId]] = {
    val queryRes = session.selectAll(
      s"""
        | select orderId, cancelled from orders where tenantId = '$tenantId'
      """.
        stripMargin)
      queryRes.map(rows =>
        rows.map { r =>
          val orderId = r.getString("orderId")
          val cancelled = r.getBool("cancelled")
          orderId
        }
      )
    }

  def selectLiveOrdersForTenant(tenantId: TenantId): Future[Seq[OrderId]] = {

    // cancelling is uncommon so we can allow filtering
    val queryRes = session.selectAll(
      s"""
         | select orderId from orders where tenantId = '$tenantId' and cancelled = FALSE allow filtering
      """.
        stripMargin)
    queryRes.map { rows =>
      val res = rows.map { r =>
        r.getString("orderId")
      }
      logger.debug(s"got live orders fo $tenantId $res")
      res
    }
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
  private var cancelOrderStatement : PreparedStatement = null

  // todo voodoo  - figure out what exactly is happening
  def aggregateTags = OrderEvent.Tag.allTags


  private def createTables() = {
    for {
      res <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS orders (
          tenantId text,
          orderId text,
          cancelled boolean,
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
      .setEventHandler[OrderCreated](e => insertOrder(e.event.tenantId, e.event.orderId, false))
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
          | INSERT INTO orders(tenantId, orderId, cancelled) VALUES (?, ?, FALSE)
        """.stripMargin)
      cancelOrder <- session.prepare(
        """
          | Update orders
          |  set cancelled = true
          |  where tenantId = ? and orderId = ?
        """.stripMargin)
    } yield {
      insertOrderStatement = insertOrder
      cancelOrderStatement = cancelOrder
      logger.debug(s"ordered insert statment $insertOrder and $insertOrderStatement")
      Done
    }
  }

  // again copy pattern from item repository
  private def insertOrder(tenantId: TenantId, orderId: OrderId, cancelled: Boolean) = {
    logger.debug(s"insertOrder $tenantId $orderId $cancelled")
    Future.successful(List(insertOrderStatement.bind(tenantId, orderId /*, new java.lang.Boolean(cancelled)*/)))
  }

  // again copy pattern from item repository
  private def cancelOrder(tenantId: TenantId, orderId: OrderId) = {
    logger.debug(s"cancelOrder $tenantId")
    val res = Future.successful(List(cancelOrderStatement.bind(tenantId, orderId)))
    logger.debug(s"cancelledOrder $tenantId")
    res
  }

}


