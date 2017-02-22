package optrak.lagomtest.orders.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import optrak.lagomtest.data.Data.{PlanDescription, Order}
import optrak.lagomtest.orders.impl.OrderEvents._
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}
import OrderTestCommon._

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class OrderEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll with OptionValues {

  private val system = ActorSystem("OrderEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(OrderSerializerRegistry))

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }



  val tenantId = "tenant1"
  val modelId = UUIDs.timeBased()

  private def withTestDriver[T](block: PersistentEntityTestDriver[OrderCommand, OrderEvent, Option[Order]] => T): T = {
    val driver = new PersistentEntityTestDriver(system, new OrderEntity, s"$tenantId:$order1Id")
    try {
      block(driver)
    } finally {
      driver.getAllIssues shouldBe empty
    }
  }

  "Order entity" should {

    "allow creation of Order" in withTestDriver { driver =>
      val outcome = driver.run(createOrder1)
      outcome.replies === Vector(Done)
      outcome.events should contain(order1Created)
      outcome.state === Some(order1)
    }

    "fail on creation of Order twice" in withTestDriver { driver =>
      val outcome = driver.run(createOrder1)
      a [OrderAlreadyExistsException] should be thrownBy driver.run(createOrder1)
    }


    "get back created order" in withTestDriver { driver =>
      driver.run(createOrder1)
      val outcome = driver.run(GetOrder)
      outcome.replies === Vector(order1)
    }

    "change size" in withTestDriver { driver =>
      driver.run(createOrder1)
      driver.run(UpdateOrderQuantity(tenantId, order1Id, 9))
      val outcome = driver.run(GetOrder)
      outcome.replies === Vector(order1sz9)
    }


  }
}
