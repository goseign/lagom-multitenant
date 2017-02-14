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



  val tenantId = "client1"
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

    "get back created product" in withTestDriver { driver =>
      driver.run(CreateProduct(tenantId, product1Id, product1Size, group1))
      val outcome = driver.run(GetProduct)
      outcome.replies === Vector(product1)


    }



  }
}
