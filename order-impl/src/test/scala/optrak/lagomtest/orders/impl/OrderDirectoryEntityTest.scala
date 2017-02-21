package optrak.lagomtest.orders.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import optrak.lagomtest.datamodel.Models.Order
import optrak.lagomtest.orders.impl.OrderEvents.{OrderCreated, OrderEvent}
import optrak.lagomtest.orders.impl.OrderTestCommon._
import optrak.lagomtest.orders.impl.TenantOrderDirectoryEntity.Innards
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}

/**
  * Created by tim on 18/02/17.
  * Copyright Tim Pigden, Hertford UK
  */
class OrderDirectoryEntityTest extends WordSpec with Matchers with BeforeAndAfterAll with OptionValues {

  private val system = ActorSystem("OrderDirectoryEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(OrderSerializerRegistry))

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  val tenantId = "tenant1"
  val modelId = UUIDs.timeBased()

  private def withTestDriver[T](block: PersistentEntityTestDriver[OrderDirectoryCommand, OrderDirectoryEvent, Innards] => T): T = {
    val driver = new PersistentEntityTestDriver(system, new TenantOrderDirectoryEntity, s"$tenantId")
    try {
      block(driver)
    } finally {
      driver.getAllIssues shouldBe empty
    }
  }

  "Order directory entity" should {

    "allow creation of Order" in withTestDriver { driver =>
      val outcome = driver.run(WrappedCreateOrder(order1Id))
      outcome.replies === Vector(Done)
      outcome.events should contain(OrderAddedToDirectory(order1Id))
      outcome.state === Some(order1)
    }

    "get back created order" in withTestDriver { driver =>
      driver.run(WrappedCreateOrder(order1Id))
      val outcome1 = driver.run(GetAllOrders)
      outcome1.replies === Vector(Seq(List(order1Id)))
      outcome1.replies === Vector(Seq(order1Id))
    }


  }
}

