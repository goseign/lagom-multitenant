package com.optrak.products.impl

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import com.optrak.products.api.Product
import optrak.lagomtest.model.impl.ClientEvents.{ProductChanged, ProductEvent}
import optrak.lagomtest.model.api.{Product, ProductUpdate}
import optrak.lagomtest.model.impl._
import org.specs2.matcher.MatchResult
import org.specs2.mutable.{BeforeAfter, Specification}

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ProductSpec extends Specification with BeforeAfter {

  private val system = ActorSystem("ProductEntitySpec")

  override def after: Any = {
    TestKit.shutdownActorSystem(system)
  }


  override def before: Any = {}

  private def withTestDriver(block: PersistentEntityTestDriver[ProductCommand, ProductEvent, ProductState] => MatchResult[_]): MatchResult[_] = {
    val driver = new PersistentEntityTestDriver(system, new ModelEntity, "product-test-1")
    block(driver)
    driver.getAllIssues should have size 0
  }

  "product entity" should {

    "return empty when nothing there yet" in withTestDriver { driver =>
      val outcome = driver.run(GetProduct)
      outcome.replies === Vector(EmptyProduct)
    }

    "allow setting of product" in withTestDriver { driver =>
      val outcome1 = driver.run(SetProduct(ProductUpdate(1, "group1")))
      outcome1.events should contain(ProductChanged(Product("product-test-1", 1, "group1")))
      val outcome2 = driver.run(GetProduct)
      outcome2.replies should contain(WithProduct(Product("product-test-1", 1, "group1")))
      val outcome3 = driver.run(SetProduct(ProductUpdate(1, "group2")))
      outcome3.events should contain(ProductChanged(Product("product-test-1", 1, "group2")))
      val outcome4 = driver.run(GetProduct)
      outcome4.replies should contain(WithProduct(Product("product-test-1", 1, "group2")))
    }

  }
}
