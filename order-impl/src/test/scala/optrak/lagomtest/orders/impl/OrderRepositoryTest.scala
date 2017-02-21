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
import optrak.lagomtest.orders.impl.OrderEvents.{OrderCreated, OrderEvent}

import scala.concurrent.Future


class OrderRepositoryTest extends AsyncWordSpec with BeforeAndAfterAll with Matchers {

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
    new LagomApplication(ctx) with OrderComponents with AhcWSComponents {
      override def serviceLocator = NoServiceLocator
      override lazy val readSide: ReadSideTestDriver = new ReadSideTestDriver
    }
  }

  override def afterAll() = server.stop()

  private val testDriver = server.application.readSide
  private val orderRepository = server.application.orderRepository
  private val offset = new AtomicInteger()


  "The order event processor" should {
    "create a order" in {
      val orderCreated = OrderCreated(tenantId, order1Id, site1, product1, quantity1)
      for {
        _ <- feed(entityId(tenantId, order1Id), orderCreated)
        orders <- getOrders
      } yield {
        orders.ids should contain only (orderCreated.orderId)
      }
    }

    "create another order" in {
      val orderCreated = OrderCreated(tenantId, order2Id, site2, product2, quantity2)
      for {
        _ <- feed(entityId(tenantId, order2Id), orderCreated)
        allOrders <- getOrders
      } yield {
        allOrders.ids should contain (order1Id)
      }
    }
  }

  private def getOrders = {
    orderRepository.selectOrdersForTenant(tenantId)
  }

  private def feed(orderId: OrderId, event: OrderEvent) = {
    testDriver.feed(orderId.toString, event, Sequence(offset.getAndIncrement))
  }


}
