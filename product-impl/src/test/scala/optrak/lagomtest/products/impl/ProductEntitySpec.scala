package optrak.lagomtest.products.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import optrak.lagomtest.datamodel.Models.{ModelDescription, Product}
import optrak.lagomtest.products.impl.ProductEvents._
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}
import ProductTestCommon._

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ProductEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll with OptionValues {

  private val system = ActorSystem("ProductEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(ProductSerializerRegistry))

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }



  val tenantId = "tenant1"
  val modelId = UUIDs.timeBased()

  private def withTestDriver[T](block: PersistentEntityTestDriver[ProductCommand, ProductEvent, Option[Product]] => T): T = {
    val driver = new PersistentEntityTestDriver(system, new ProductEntity, s"$tenantId:$product1Id")
    try {
      block(driver)
    } finally {
      driver.getAllIssues shouldBe empty
    }
  }

  "Product entity" should {

    "allow creation of Product" in withTestDriver { driver =>
      val outcome = driver.run(CreateProduct(tenantId, product1Id, product1Size, group1))
      outcome.replies === Vector(Done)
      outcome.events should contain(ProductCreated(tenantId, product1Id, product1Size, group1))
      outcome.state === Some(product1)
    }

    "fail on creation of Product twice" in withTestDriver { driver =>
      val outcome = driver.run(CreateProduct(tenantId, product1Id, product1Size, group1))
      a [ProductAlreadyExistsException] should be thrownBy driver.run(CreateProduct(tenantId, product1Id, product1Size, group1))
    }


    "get back created product" in withTestDriver { driver =>
      driver.run(CreateProduct(tenantId, product1Id, product1Size, group1))
      val outcome = driver.run(GetProduct)
      outcome.replies === Vector(product1)
    }

    "change size" in withTestDriver { driver =>
      driver.run(CreateProduct(tenantId, product1Id, product1Size, group1))
      driver.run(UpdateProductSize(tenantId, product1Id, 9))
      val outcome = driver.run(GetProduct)
      outcome.replies === Vector(product1sz9)
    }

    "change group" in withTestDriver { driver =>
      driver.run(CreateProduct(tenantId, product1Id, product1Size, group1))
      driver.run(UpdateProductGroup(tenantId, product1Id, group2))
      val outcome = driver.run(GetProduct)
      outcome.replies === Vector(product1g2)
    }

    "cancel product" in withTestDriver { driver =>
      driver.run(CreateProduct(tenantId, product1Id, product1Size, group1))
      driver.run(CancelProduct(tenantId, product1Id))
      val outcome = driver.run(GetProduct)
      outcome.replies === Vector(product1Cancelled)
    }




  }
}
