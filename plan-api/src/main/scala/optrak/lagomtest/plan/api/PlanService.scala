package optrak.lagomtest.plan.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import optrak.lagomtest.data.Data._
import optrak.lagomtest.data.DataJson._
import optrak.lagomtest.plan.api.PlanEvents._
import play.api.libs.json.{Format, Json}

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */

object PlanService {
  def topicName = "PlanTopic"


  case class SimplePlan(
                         id: PlanId,
                         description: String,
                         products: Set[Product],
                         sites: Set[Site],
                         orders: Set[Order],
                         vehicles: Set[Vehicle],
                         trips: Set[Trip])

  implicit val formatPlan: Format[SimplePlan] = Json.format[SimplePlan]

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

  def createPlan(planId: PlanId): ServiceCall[String, Done]

  // product CRUD
  def addProduct(planId: PlanId): ServiceCall[Product, Done]

  def updateProduct(planId: PlanId): ServiceCall[Product, Done]

  def addOrUpdateProduct(planId: PlanId): ServiceCall[Product, Done]

  def removeProduct(planId: PlanId): ServiceCall[ProductId, Done]

  // Site CRUD

  def addSite(planId: PlanId): ServiceCall[Site, Done]

  def updateSite(planId: PlanId): ServiceCall[Site, Done]

  def addOrUpdateSite(planId: PlanId): ServiceCall[Site, Done]

  def removeSite(planId: PlanId): ServiceCall[SiteId, Done]

  // query methods, getting one or all the products from the plan
  
  def product(planId: PlanId, productId: ProductId): ServiceCall[NotUsed, Product]

  def products(planId: PlanId): ServiceCall[NotUsed, Seq[Product]]

  def site(planId: PlanId, siteId: SiteId): ServiceCall[NotUsed, Site]

  def sites(planId: PlanId): ServiceCall[NotUsed, Seq[Site]]


  override final def descriptor = {
    import Service._

    named("plan").withCalls(
      pathCall("/optrak.plan.api/createPlan/:id", createPlan _),

      pathCall("/optrak.plan.api/addProduct/:id", addProduct _),
      pathCall("/optrak.plan.api/updateProduct/:id", updateProduct _),
      pathCall("/optrak.plan.api/addOrUpdateProduct/:id", addOrUpdateProduct _),
      pathCall("/optrak.plan.api/removeProduct/:id", removeProduct _),

      pathCall("/optrak.plan.api/addSite/:id", addSite _),
      pathCall("/optrak.plan.api/updateSite/:id", updateSite _),
      pathCall("/optrak.plan.api/addOrUpdateSite/:id", addOrUpdateSite _),
      pathCall("/optrak.plan.api/removeSite/:id", removeSite _),

      pathCall("/optrak.plan.api/product/:planId/:productId", product _),
      pathCall("/optrak.plan.api/product/:planId", products _),
      pathCall("/optrak.plan.api/site/:planId/:siteId", site _),
      pathCall("/optrak.plan.api/site/:planId", sites _)


    ).withTopics(
      topic(PlanService.topicName, planEvents)
      .addProperty(KafkaProperties.partitionKeyStrategy,
      PartitionKeyStrategy[PlanEvent](_.planId.toString))
    )
    .withAutoAcl(true)

  }
  def planEvents() : Topic[PlanEvent]

}

