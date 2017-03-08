package optrak.lagomtest.plan.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import optrak.lagomtest.data.Data._
import optrak.lagomtest.plan.api.PlanEntityErrors._
import optrak.lagomtest.plan.api.PlanService.CheckedResult
import optrak.scalautils.json.JsonImplicits._
import optrak.lagom.utils .PlayJson4s._

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */

object PlanService {
  def topicName = "PlanTopic"

  trait ErrorMessage extends Throwable {
    def planId: PlanId
  }

  case class CheckedResult[T <: ErrorMessage](result: Option[T])

  object CheckedResult {
    def empty[T <: ErrorMessage] = new CheckedResult[T](None)
  }

  case class SimplePlan(
                         id: PlanId,
                         description: String,
                         products: Set[Product],
                         sites: Set[Site],
                         orders: Set[Order],
                         vehicles: Set[Vehicle],
                         trips: Set[Trip])

  object SimplePlan {
    def apply(planDescription: PlanDescription): SimplePlan =
      new SimplePlan(planDescription.id, planDescription.description, Set.empty, Set.empty, Set.empty, Set.empty, Set.empty)

    def apply(plan: Plan) : SimplePlan = {
      import plan._
      new SimplePlan(id, description, products, sites, orders, vehicles, trips)
    }

  }

}

trait PlanService extends Service {

  def createPlan(planId: PlanId): ServiceCall[String, CheckedResult[PlanAlreadyExistsError]]

  // product CRUD
  def addProduct(planId: PlanId): ServiceCall[Product, Done]

  def updateProduct(planId: PlanId): ServiceCall[Product, CheckedResult[ProductNotDefinedError]]

  def removeProduct(planId: PlanId): ServiceCall[ProductId, Done]

  // Site CRUD

  def addSite(planId: PlanId): ServiceCall[Site, Done]

  def updateSite(planId: PlanId): ServiceCall[Site, Done]

  def removeSite(planId: PlanId): ServiceCall[SiteId, Done]

  def addOrder(planId: PlanId): ServiceCall[Order, Done]

  def updateOrder(planId: PlanId): ServiceCall[Order, Done]

  def removeOrder(planId: PlanId): ServiceCall[OrderId, Done]

  def addVehicle(planId: PlanId): ServiceCall[Vehicle, Done]

  def updateVehicle(planId: PlanId): ServiceCall[Vehicle, Done]

  def removeVehicle(planId: PlanId): ServiceCall[VehicleId, Done]

  // query methods, getting one or all the products from the plan
  
  def product(planId: PlanId, productId: ProductId): ServiceCall[NotUsed, Product]

  def products(planId: PlanId): ServiceCall[NotUsed, Seq[Product]]

  def site(planId: PlanId, siteId: SiteId): ServiceCall[NotUsed, Site]

  def sites(planId: PlanId): ServiceCall[NotUsed, Seq[Site]]

  def vehicle(planId: PlanId, vehicleId: VehicleId): ServiceCall[NotUsed, Vehicle]

  def vehicles(planId: PlanId): ServiceCall[NotUsed, Seq[Vehicle]]

  def order(planId: PlanId, orderId: OrderId): ServiceCall[NotUsed, Order]

  def orders(planId: PlanId): ServiceCall[NotUsed, Seq[Order]]


  override final def descriptor = {
    import Service._

    named("plan").withCalls(
      // pathCall("/optrak.plan.api/createPlan/:id", createPlan _),

      pathCall("/optrak.plan.api/addProduct/:id", addProduct _),
      // pathCall("/optrak.plan.api/updateProduct/:id", updateProduct _),
      pathCall("/optrak.plan.api/removeProduct/:id", removeProduct _),

      pathCall("/optrak.plan.api/addSite/:id", addSite _),
      pathCall("/optrak.plan.api/updateSite/:id", updateSite _),
      pathCall("/optrak.plan.api/removeSite/:id", removeSite _),

      pathCall("/optrak.plan.api/addVehicle/:id", addVehicle _),
      pathCall("/optrak.plan.api/updateVehicle/:id", updateVehicle _),
      pathCall("/optrak.plan.api/removeVehicle/:id", removeVehicle _),

      pathCall("/optrak.plan.api/addOrder/:id", addOrder _),
      pathCall("/optrak.plan.api/updateOrder/:id", updateOrder _),
      pathCall("/optrak.plan.api/removeOrder/:id", removeOrder _),

      pathCall("/optrak.plan.api/product/:planId/:productId", product _),
      pathCall("/optrak.plan.api/product/:planId", products _),
      pathCall("/optrak.plan.api/site/:planId/:siteId", site _),
      pathCall("/optrak.plan.api/site/:planId", sites _),

      pathCall("/optrak.plan.api/order/:planId/:orderId", order _),
      pathCall("/optrak.plan.api/order/:planId", orders _),

      pathCall("/optrak.plan.api/vehicle/:planId/:vehicleId", vehicle _),
      pathCall("/optrak.plan.api/vehicle/:planId", vehicles _)


/*
    ).withTopics(
      topic(PlanService.topicName, planEvents)
      .addProperty(KafkaProperties.partitionKeyStrategy,
      PartitionKeyStrategy[PlanEvent](_.planId.toString))
*/
    )
    .withAutoAcl(true)

  }
  // def planEvents() : Topic[PlanEvent]

}

