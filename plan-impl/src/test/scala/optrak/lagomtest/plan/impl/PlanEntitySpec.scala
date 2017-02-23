package optrak.lagomtest.plan.impl

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import optrak.lagomtest.data.Data._
import optrak.lagomtest.plan.PlanCommands._
import optrak.lagomtest.plan.{PlanEntity, PlanSerializerRegistry}
import optrak.lagomtest.plan.api.PlanEvents._
import optrak.lagomtest.plan.api.PlanImpl
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}
import optrak.lagomtest.products.impl.ProductTestCommon._
import optrak.lagomtest.sites.impl.SiteTestCommon._
import optrak.lagomtest.vehicles.impl.VehicleTestCommon._
import optrak.lagomtest.orders.impl.OrderTestCommon._
import optrak.lagomtest.plan.api.PlanEntityErrors._
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class PlanEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll with OptionValues {

  private val system = ActorSystem("TenantEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(PlanSerializerRegistry))

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  val planId = UUIDs.timeBased()
  val hello = "hellow"
  val planDescription = PlanDescription(planId, hello)

  private def withTestDriver[T](block: PersistentEntityTestDriver[PlanCommand, PlanEvent, Option[PlanImpl]] => T): T= {
    val driver = new PersistentEntityTestDriver(system, new PlanEntity, planId.toString)
    try {
      block(driver)
    } finally {
      driver.getAllIssues shouldBe empty
    }
  }

  "Tenant entity" should {

    "allow creation of plan" in withTestDriver { driver =>
      val outcome = driver.run(CreatePlan(planDescription))
      outcome.replies should === (Vector(Done))
      outcome.events should contain(PlanCreated(planDescription))
      outcome.state should === (Some(PlanImpl(planDescription)))
    }
  }

  "Products" should {

    "add product" in withTestDriver { driver =>
      val outcome1 = driver.run(CreatePlan(planDescription))
      val outcome = driver.run(AddProduct(planId, product1))
      outcome.replies should === (Vector(Done))
      outcome.events should contain(ProductUpdated(planId, product1))
      outcome.state should === (Some(PlanImpl(PlanDescription(planId, hello), productsM = Map(product1.id -> product1))))
    }

    "update product" in withTestDriver { driver =>
      driver.run(CreatePlan(planDescription))
      driver.run(AddProduct(planId, product1))
      val outcome = driver.run(UpdateProduct(planId, product1sz9))
      outcome.replies should === (Vector(Done))
      outcome.events should contain(ProductUpdated(planId, product1sz9))
      outcome.state should === (Some(PlanImpl(PlanDescription(planId, hello), productsM = Map(product1.id -> product1sz9))))
    }

    "update non-existent product" in withTestDriver { driver =>
      driver.run(CreatePlan(planDescription))
      a [ProductNotDefinedError] should be thrownBy driver.run(UpdateProduct(planId, product1sz9))
    }



    "remove product " in withTestDriver { driver =>
      val outcome1 = driver.run(CreatePlan(planDescription))
      val outcome2 = driver.run(AddProduct(planId, product1))

      val outcome = driver.run(RemoveProduct(planId, product1.id))
      outcome.replies should === (Vector(Done))
      outcome.events should contain(ProductRemoved(planId, product1.id))
      outcome.state should === (Some(PlanImpl(planDescription)))
    }

    "remove non existent does nothing" in withTestDriver { driver =>
      driver.run(CreatePlan(planDescription))
      val outcome = driver.run(RemoveProduct(planId, product1.id))
      outcome.replies should === (Vector(Done))
      outcome.events should contain(ProductRemoved(planId, product1.id))
      outcome.state should === (Some(PlanImpl(planDescription)))
    }

  }

  "Sites" should {

    "add site" in withTestDriver { driver =>
      val outcome1 = driver.run(CreatePlan(planDescription))
      val outcome = driver.run(AddSite(planId, site1))
      outcome.replies should === (Vector(Done))
      outcome.events should contain(SiteUpdated(planId, site1))
      outcome.state should === (Some(PlanImpl(PlanDescription(planId, hello), sitesM = Map(site1.id -> site1))))
    }

    "update site" in withTestDriver { driver =>
      driver.run(CreatePlan(planDescription))
      driver.run(AddSite(planId, site1))
      val outcome = driver.run(UpdateSite(planId, site1g2))
      outcome.replies should === (Vector(Done))
      outcome.events should contain(SiteUpdated(planId, site1g2))
      outcome.state should === (Some(PlanImpl(PlanDescription(planId, hello), sitesM = Map(site1.id -> site1g2))))
    }

    "update non-existent site" in withTestDriver { driver =>
      driver.run(CreatePlan(planDescription))
      a [SiteNotDefinedError] should be thrownBy driver.run(UpdateSite(planId, site1g2))
    }

    "remove site " in withTestDriver { driver =>
      val outcome1 = driver.run(CreatePlan(planDescription))
      val outcome2 = driver.run(AddSite(planId, site1))

      val outcome = driver.run(RemoveSite(planId, site1.id))
      outcome.replies should === (Vector(Done))
      outcome.events should contain(SiteRemoved(planId, site1.id))
      outcome.state should === (Some(PlanImpl(planDescription)))
    }

    "remove non existent does nothing" in withTestDriver { driver =>
      driver.run(CreatePlan(planDescription))
      val outcome = driver.run(RemoveSite(planId, site1.id))
      outcome.replies should === (Vector(Done))
      outcome.events should contain(SiteRemoved(planId, site1.id))
      outcome.state should === (Some(PlanImpl(planDescription)))
    }
  }

  "Vehicles" should {

    "add vehicle" in withTestDriver { driver =>
      val outcome1 = driver.run(CreatePlan(planDescription))
      val outcome = driver.run(AddVehicle(planId, vehicle1))
      outcome.replies should === (Vector(Done))
      outcome.events should contain(VehicleUpdated(planId, vehicle1))
      outcome.state should === (Some(PlanImpl(PlanDescription(planId, hello), vehiclesM = Map(vehicle1.id -> vehicle1))))
    }

    "update vehicle" in withTestDriver { driver =>
      driver.run(CreatePlan(planDescription))
      driver.run(AddVehicle(planId, vehicle1))
      val outcome = driver.run(UpdateVehicle(planId, vehicle1g2))
      outcome.replies should === (Vector(Done))
      outcome.events should contain(VehicleUpdated(planId, vehicle1g2))
      outcome.state should === (Some(PlanImpl(PlanDescription(planId, hello), vehiclesM = Map(vehicle1.id -> vehicle1g2))))
    }

    "update non-existent vehicle" in withTestDriver { driver =>
      driver.run(CreatePlan(planDescription))
      a [VehicleNotDefinedError] should be thrownBy driver.run(UpdateVehicle(planId, vehicle1g2))
    }


    "remove vehicle " in withTestDriver { driver =>
      val outcome1 = driver.run(CreatePlan(planDescription))
      val outcome2 = driver.run(AddVehicle(planId, vehicle1))

      val outcome = driver.run(RemoveVehicle(planId, vehicle1.id))
      outcome.replies should === (Vector(Done))
      outcome.events should contain(VehicleRemoved(planId, vehicle1.id))
      outcome.state should === (Some(PlanImpl(planDescription)))
    }
    "remove non existent does nothing" in withTestDriver { driver =>
      driver.run(CreatePlan(planDescription))
      val outcome = driver.run(RemoveVehicle(planId, vehicle1.id))
      outcome.replies should === (Vector(Done))
      outcome.events should contain(VehicleRemoved(planId, vehicle1.id))
      outcome.state should === (Some(PlanImpl(planDescription)))
    }

  }

  "Orders" should {

    "add order no product fails"  in withTestDriver { driver =>
      val outcome1 = driver.run(CreatePlan(planDescription))
      a [ProductNotDefinedError] should be thrownBy driver.run(AddOrder(planId, order1))
    }

    "add order no site fails"  in withTestDriver { driver =>
      val outcome1 = driver.run(CreatePlan(planDescription))
      driver.run(AddProduct(planId, product1))
      a [SiteNotDefinedError] should be thrownBy driver.run(AddOrder(planId, order1))
    }

    "add order" in withTestDriver { driver =>
      driver.run(CreatePlan(planDescription))
      driver.run(AddProduct(planId, product1))
      driver.run(AddSite(planId, site1))
      val outcome = driver.run(AddOrder(planId, order1))
      outcome.replies should === (Vector(Done))
      outcome.events should contain(OrderUpdated(planId, order1))
      outcome.state should === (Some(PlanImpl(
        PlanDescription(planId, hello),
        productsM = Map(product1.id -> product1),
        ordersM = Map(order1.id -> order1),
        sitesM = Map(site1.id -> site1))))
    }

    "update order" in withTestDriver { driver =>
      driver.run(CreatePlan(planDescription))
      driver.run(AddProduct(planId, product1))
      driver.run(AddSite(planId, site1))
      driver.run(AddOrder(planId, order1))
      val outcome = driver.run(UpdateOrder(planId, order1sz9))
      outcome.replies should === (Vector(Done))
      outcome.events should contain(OrderUpdated(planId, order1sz9))
      outcome.state should === (Some(PlanImpl(
        PlanDescription(planId, hello),
        productsM = Map(product1.id -> product1),
        ordersM = Map(order1.id -> order1sz9),
        sitesM = Map(site1.id -> site1))))
    }

    "update non-existent order" in withTestDriver { driver =>
      driver.run(CreatePlan(planDescription))
      driver.run(AddProduct(planId, product1))
      driver.run(AddSite(planId, site1))
      a [OrderNotDefinedError] should be thrownBy driver.run(UpdateOrder(planId, order1sz9))
    }

    "remove order " in withTestDriver { driver =>
      val outcome1 = driver.run(CreatePlan(planDescription))
      driver.run(AddProduct(planId, product1))
      driver.run(AddSite(planId, site1))
      val outcome2 = driver.run(AddOrder(planId, order1))

      val outcome = driver.run(RemoveOrder(planId, order1.id))
      outcome.replies should === (Vector(Done))
      outcome.events should contain(OrderRemoved(planId, order1.id))
      outcome.state should === (Some(PlanImpl(
          PlanDescription(planId, hello),
          productsM = Map(product1.id -> product1),
          sitesM = Map(site1.id -> site1))))
    }
    "remove non existent does nothing" in withTestDriver { driver =>
      driver.run(CreatePlan(planDescription))
      val outcome = driver.run(RemoveOrder(planId, order1.id))
      outcome.replies should === (Vector(Done))
      outcome.events should contain(OrderRemoved(planId, order1.id))
      outcome.state should === (Some(PlanImpl(planDescription)))
    }

    "remove referenced product fails" in withTestDriver { driver =>
      driver.run(CreatePlan(planDescription))
      driver.run(AddProduct(planId, product1))
      driver.run(AddSite(planId, site1))
      driver.run(AddOrder(planId, order1))
      a [ProductReferencedByOrdersError] should be thrownBy driver.run(RemoveProduct(planId, product1.id))
    }

    "remove referenced site fails" in withTestDriver { driver =>
      driver.run(CreatePlan(planDescription))
      driver.run(AddProduct(planId, product1))
      driver.run(AddSite(planId, site1))
      driver.run(AddOrder(planId, order1))
      a [SiteReferencedByOrdersError] should be thrownBy driver.run(RemoveSite(planId, site1.id))
    }

  }
}
