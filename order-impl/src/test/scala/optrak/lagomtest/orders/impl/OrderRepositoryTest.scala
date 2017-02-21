package optrak.lagomtest.orders.impl

import java.util.concurrent.atomic.AtomicInteger

import akka.persistence.query.Sequence
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server.LagomApplication
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import optrak.lagomtest.datamodel.Models.OrderId
import optrak.lagomtest.utils.ReadSideTestDriver
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.libs.ws.ahc.AhcWSComponents
import OrderTestCommon._

import scala.concurrent.Future
/*

class OrderRepositoryTest extends AsyncWordSpec with BeforeAndAfterAll with Matchers {

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
    new LagomApplication(ctx) with OrderComponents with AhcWSComponents {
      override def serviceLocator = NoServiceLocator
      override lazy val readSide: ReadSideTestDriver = new ReadSideTestDriver
    }
  }

  override def afterAll() = server.stop()

  private val testDriver = server.application.readSide
  // private val orderRepository = server.application.orderRepository
  private val offset = new AtomicInteger()


  "The order event processor" should {
    "create a order" in {
      val orderCreated = OrderCreated(tenantId, order1Id, order1Size, group1)
      for {
        _ <- feed(entityId(tenantId, order1Id), orderCreated)
        orders <- getOrders
      } yield {
        orders should contain only (OrderStatus(orderCreated.orderId, false))
      }
    }

    "create another order" in {
      val orderCreated = OrderCreated(tenantId, order2Id, order2Size, group2)
      val orderCancelled = OrderCancelled(tenantId, order1Id)
      for {
        _ <- feed(entityId(tenantId, order2Id), orderCreated)
        _ <- feed(entityId(tenantId, order1Id), orderCancelled)
        allOrders <- getOrders
        liveOrders <- getLiveOrders
      } yield {
        allOrders should contain (OrderStatus(order1Id, true))
        allOrders should contain (OrderStatus(order2Id, false))
        liveOrders should contain only (order2Id)
      }
    }
  }

  private def getOrders = {
    orderRepository.selectOrdersForTenant(tenantId)
  }

  private def getLiveOrders = {
    orderRepository.selectLiveOrdersForTenant(tenantId)
  }

  private def feed(orderId: OrderId, event: OrderEvent) = {
    testDriver.feed(orderId.toString, event, Sequence(offset.getAndIncrement))
  }


}*/
