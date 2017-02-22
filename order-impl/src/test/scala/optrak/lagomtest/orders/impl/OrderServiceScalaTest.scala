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
import org.scalacheck._
import org.scalacheck.Shapeless._
// import org.scalacheck.Gen._

import scala.concurrent.Future

class OrderServiceScalaTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new OrderApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[OrderService]

  override protected def afterAll() = server.stop()
  
  def createOrderData(order: Order) = OrderCreationData(site1, product1, quantity1)

/*
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

  }
  */
  "reading" should {
    def createP(implicit arb: Arbitrary[OrderCreationData]): Option[OrderCreationData] =
      arb.arbitrary.sample

    "create multiple orders for single tenant" in {

      implicitly[Arbitrary[OrderCreationData]]

      // we're going to generate some arbitrary orders then check that they actually got created
      val cps : List[OrderCreationData] = 0.to(10).flatMap( i => createP ).toList
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
