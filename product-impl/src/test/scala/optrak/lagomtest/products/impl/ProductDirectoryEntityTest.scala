package optrak.lagomtest.products.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import optrak.lagomtest.data.Data.Product
import optrak.lagomtest.products.api.ProductStatus
import optrak.lagomtest.products.impl.ProductEvents.{ProductCreated, ProductEvent}
import optrak.lagomtest.products.impl.ProductTestCommon._
import optrak.lagomtest.products.impl.TenantProductDirectoryEntity.Innards
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}

/**
  * Created by tim on 18/02/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ProductDirectoryEntityTest extends WordSpec with Matchers with BeforeAndAfterAll with OptionValues {

  private val system = ActorSystem("ProductDirectoryEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(ProductSerializerRegistry))

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  val tenantId = "tenant1"
  val modelId = UUIDs.timeBased()

  private def withTestDriver[T](block: PersistentEntityTestDriver[ProductDirectoryCommand, ProductDirectoryEvent, Innards] => T): T = {
    val driver = new PersistentEntityTestDriver(system, new TenantProductDirectoryEntity, s"$tenantId")
    try {
      block(driver)
    } finally {
      driver.getAllIssues shouldBe empty
    }
  }

  "Product directory entity" should {

    "allow creation of Product" in withTestDriver { driver =>
      val outcome = driver.run(WrappedCreateProduct(product1Id))
      outcome.replies === Vector(Done)
      outcome.events should contain(ProductAddedToDirectory(product1Id))
      outcome.state === Some(product1)
    }

    "get back created product" in withTestDriver { driver =>
      driver.run(WrappedCreateProduct(product1Id))
      val outcome1 = driver.run(GetAllProducts)
      outcome1.replies === Vector(Seq(ProductStatus(product1Id, false)))
      val outcome2 = driver.run(GetLiveProducts)
      outcome1.replies === Vector(Seq(product1Id))
    }

    "cancel product" in withTestDriver { driver =>
      driver.run(WrappedCreateProduct(product1Id))
      driver.run(WrappedCancelProduct(product1Id))
      val outcome1 = driver.run(GetAllProducts)
      outcome1.replies === Vector(Seq(ProductStatus(product1Id, true)))
      val outcome2 = driver.run(GetLiveProducts)
      outcome1.replies === Vector(Seq.empty)
    }




  }
}

