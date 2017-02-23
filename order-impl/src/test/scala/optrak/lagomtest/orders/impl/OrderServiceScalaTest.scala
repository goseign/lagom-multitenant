package optrak.lagomtest.orders.impl

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.TransportException
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import optrak.lagomtest.orders.api.{OrderCreationData, OrderIds, OrderService}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import OrderTestCommon._
import optrak.lagomtest.data.Data.{Order, OrderId}
import optrak.lagomtest.products.api.ProductService
import optrak.lagomtest.sites.api.SiteService
import org.scalacheck._
import org.scalacheck.Shapeless._
// import org.scalacheck.Gen._

import scala.concurrent.Future

class OrderServiceScalaTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new OrderApplication(ctx) with LocalServiceLocator {
      override lazy val productService: ProductService = ProductMock(Set(product1Id, product2Id))
      override lazy val siteService: SiteService = SiteMock(Set(site1Id, site2Id))
    }
  }

  val client = server.serviceClient.implement[OrderService]

  override protected def afterAll() = server.stop()
  
  def createOrderData(order: Order) = OrderCreationData(order.site, order.product, order.quantity)


  "order service" should {

    "create and retrieve order" in {
      for {
        answer <- client.createOrder(tenantId, order1Id).invoke(createOrderData(order1))
        retrieved <- client.getOrder(tenantId, order1Id).invoke()
      } yield {
        answer should ===(Done)
      }
    }


    "complain about 2nd attempt create order" in {
      val exp = recoverToExceptionIf[TransportException](
      for {
        answer <- client.createOrder(tenantId, order1Id).invoke(createOrderData(order1))
        answer2 <- client.createOrder(tenantId, order1Id).invoke(createOrderData(order1))
      } yield {
        answer2 should ===(Done)
      })
      exp.map { te =>
        // println(s"te is code ${te.errorCode} message ${te.exceptionMessage}")
        te.toString should include("order order1 for tenant tenant1 already exists")
      }
    }


    "complain about bad product" in {
      val exp = recoverToExceptionIf[TransportException](
        for {
          answer <- client.createOrder(tenantId, "badProduct").invoke(createOrderData(order1.copy(product = "junk")))
        } yield {
          answer should ===(Done)
        })
      exp.map { te =>
        // println(s"te is code ${te.errorCode} message ${te.exceptionMessage}")
        te.toString should include("Product tenant1:junk not found")
      }
    }
    "complain about bad site" in {
      val exp = recoverToExceptionIf[TransportException](
        for {
          answer <- client.createOrder(tenantId, "badSite").invoke(createOrderData(order1.copy(site = "junk")))
        } yield {
          answer should ===(Done)
        })
      exp.map { te =>
        // println(s"te is code ${te.errorCode} message ${te.exceptionMessage}")
        te.toString should include("Site tenant1:junk not found")
      }
    }

  }
  "reading" should {
    def createO(i: Int): OrderCreationData =
      OrderCreationData(site1Id, product1Id, i)

    "create multiple orders for single tenant" in {

      // we're going to generate some arbitrary orders then check that they actually got created
      val cps : List[OrderCreationData] = 0.to(10).map( i => createO(i) ).toList
      val orders: List[(OrderId, OrderCreationData)] = cps.zipWithIndex.map(t => (t._2.toString, t._1))
      val ordersCreated = orders.map {t =>
        Thread.sleep(1500)
        client.createOrder(tenantId, t._1).invoke(t._2)
      }

      for {
        seq <- Future.sequence(ordersCreated)
        allOrders <- {
          Thread.sleep(4000)
          client.getOrdersForTenant(tenantId).invoke()
        }
      } yield {
        val ap: OrderIds = allOrders
        println(s"all orders is $allOrders")

        orders.foreach { p =>
          val found = ap.ids.find(_ == p._1)
          found should === (Some(p._1))
        }
        true should === (true)
      }

    }
  }

}
