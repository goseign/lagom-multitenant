package optrak.lagomtest.plan

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import optrak.lagomtest.data.Data
import optrak.lagomtest.data.Data._
import optrak.lagomtest.plan.api.PlanEvents._
import optrak.lagomtest.plan.api.{PlanEvents, PlanService}
import optrak.lagomtest.plan.PlanCommands._

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  * Most methods go straight through to the  plan entity.
  */
class PlanServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends PlanService {

  /**
    * most messages go straight through to the entity
    */
  private def toPlan[Request](planId: PlanId, f: (PlanId, Request) => PlanCommand): ServiceCall[Request, Done] =
    ServiceCall { request =>
      val ref = persistentEntityRegistry.refFor[PlanEntity](planId.toString)
      ref.ask(f(planId, request))
    }

  override def createPlan(planId: PlanId): ServiceCall[String, Done] =
    toPlan(planId, ( (mid, req) => CreatePlan(PlanDescription(mid, req))))

  override def addProduct(planId: PlanId): ServiceCall[Data.Product, Done] =
    toPlan(planId, AddProduct)


  override def updateProduct(planId: PlanId): ServiceCall[Data.Product, Done] =
    toPlan(planId, UpdateProduct)

  override def addOrUpdateProduct(planId: PlanId): ServiceCall[Data.Product, Done] =
    toPlan(planId, AddOrUpdateProduct)

  override def removeProduct(planId: PlanId): ServiceCall[ProductId, Done] =
    toPlan(planId, RemoveProduct)

  override def addSite(planId: PlanId): ServiceCall[Data.Site, Done] =
    toPlan(planId, AddSite)

  override def updateSite(planId: PlanId): ServiceCall[Data.Site, Done] =
    toPlan(planId, UpdateSite)

  override def addOrUpdateSite(planId: PlanId): ServiceCall[Data.Site, Done] =
    toPlan(planId, AddOrUpdateSite)

  override def removeSite(planId: PlanId): ServiceCall[SiteId, Done] =
    toPlan(planId, RemoveSite)


  override def planEvents(): Topic[PlanEvents.PlanEvent] = TopicProducer.taggedStreamWithOffset(PlanEvent.Tag.allTags.toList) { (tag, offset) =>
    persistentEntityRegistry.eventStream(tag, offset).map(t => (t.event, offset))
  }

  override def product(planId: PlanId, productId: ProductId): ServiceCall[NotUsed, Data.Product] = ???

  override def products(planId: PlanId): ServiceCall[NotUsed, Seq[Data.Product]] = ???

  override def site(planId: PlanId, siteId: SiteId): ServiceCall[NotUsed, Data.Site] = ???

  override def sites(planId: PlanId): ServiceCall[NotUsed, Seq[Data.Site]] = ???
}
