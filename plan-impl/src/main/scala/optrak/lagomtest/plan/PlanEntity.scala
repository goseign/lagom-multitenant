package optrak.lagomtest.plan

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import optrak.lagomtest.data.Data._
import optrak.lagomtest.plan.api.PlanEvents._
import PlanEntityExceptions._
import optrak.lagomtest.plan.PlanCommands._
import optrak.lagomtest.plan.api.PlanImpl
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */

class PlanEntity extends PersistentEntity {


  override type Command = PlanCommand
  override type Event = PlanEvent
  override type State = Option[PlanImpl]

  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState = None

  /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case None => noPlanYet
    case Some(plan) => hasPlan
  }

  def noPlanYet: Actions = {
    Actions()
    .onCommand[CreatePlan, Done] {
      case (CreatePlan(planDescription), ctx, _) =>
        ctx.thenPersist(PlanCreated(planDescription))(evt =>
          ctx.reply(Done))
    }.onEvent{
      case (PlanCreated(planDescription), _) =>
        Some(PlanImpl(planDescription))
    }
  }

  def hasPlan: Actions = {
    Actions().onCommand[CreatePlan, Done] {
      case (CreatePlan(planDescription), ctx, _) =>
        throw new PlanAlreadyExistsException(planDescription)
    }.onCommand[AddOrUpdateProduct, Done] {
      case (AddOrUpdateProduct(planId, product), ctx, state) =>
        ctx.thenPersist(ProductUpdated(planId, product))(evt => ctx.reply(Done))
    }.onCommand[UpdateProduct, Done] {
      case (UpdateProduct(planId, product), ctx, Some(plan)) =>
        if (plan.productsM.contains(product.id))
          ctx.thenPersist(ProductUpdated(planId, product))(evt => ctx.reply(Done))
        else
          throw new ProductNotDefinedException(product.id, plan.id)
    }.onCommand[AddProduct, Done] {
      case (AddProduct(planId, product), ctx, Some(plan)) =>
        if (!plan.productsM.contains(product.id))
          ctx.thenPersist(ProductUpdated(planId, product))(evt => ctx.reply(Done))
        else
          throw new ProductAlreadyDefinedException(product.id, plan.id)
    }.onCommand[RemoveProduct, Done] {
      case (RemoveProduct(planId, productId), ctx, Some(plan)) =>
        ctx.thenPersist(ProductRemoved(planId, productId))(evt => ctx.reply(Done))
    }.onCommand[AddOrUpdateSite, Done] {
      case (AddOrUpdateSite(planId, site), ctx, state) =>
        ctx.thenPersist(SiteUpdated(planId, site))(evt => ctx.reply(Done))
    }.onCommand[UpdateSite, Done] {
      case (UpdateSite(planId, site), ctx, Some(plan)) =>
        if (plan.sitesM.contains(site.id))
          ctx.thenPersist(SiteUpdated(planId, site))(evt => ctx.reply(Done))
        else
          throw new SiteNotDefinedException(site.id, plan.id)
    }.onCommand[AddSite, Done] {
      case (AddSite(planId, site), ctx, Some(plan)) =>
        if (!plan.sitesM.contains(site.id))
          ctx.thenPersist(SiteUpdated(planId, site))(evt => ctx.reply(Done))
        else
          throw new SiteAlreadyDefinedException(site.id, plan.id)
    }.onCommand[RemoveSite, Done] {
      case (RemoveSite(planId, siteId), ctx, Some(plan)) =>
        ctx.thenPersist(SiteRemoved(planId, siteId))(evt => ctx.reply(Done))
    }.onEvent {
      case (ProductUpdated(planId, newProduct), Some(plan)) =>
        Some(plan.copy(productsM = plan.productsM + (newProduct.id -> newProduct)))
    }.onEvent {
      case (ProductRemoved(planId, productId), Some(plan)) =>
        Some(plan.copy(productsM = plan.productsM - productId))
    }.onEvent {
      case (SiteUpdated(planId, newSite), Some(plan)) =>
        Some(plan.copy(sitesM = plan.sitesM + (newSite.id -> newSite)))
    }.onEvent {
      case (SiteRemoved(planId, siteId), Some(plan)) =>
        Some(plan.copy(sitesM = plan.sitesM - siteId))
    }
  }
}





