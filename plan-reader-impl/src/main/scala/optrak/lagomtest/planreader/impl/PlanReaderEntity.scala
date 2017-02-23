package optrak.lagomtest.planreader.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import optrak.lagomtest.data.Data._
import optrak.lagomtest.plan.api.{PlanEvents, PlanImpl}
import optrak.lagomtest.plan.api.PlanEvents._
import optrak.lagomtest.plan.api.PlanService.SimplePlan
import optrak.lagomtest.utils.JsonFormats
import play.api.libs.json.{Format, Reads, Writes}

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  * NB this uses a one-to-one map of event to internal event. Why not?
  */


class PlanReaderEntity extends PersistentEntity {

  override type Command = PlanReaderCommand
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
      .onCommand[WrappedPlanEvent, Done] {
      case (WrappedPlanEvent(PlanCreated(planDescription)), ctx, _) =>
        ctx.thenPersist(PlanCreated(planDescription))(evt =>
          ctx.reply(Done))
    }.onEvent{
      case (PlanCreated(planDescription), _) =>
        Some(PlanImpl(planDescription))
    }
  }

  def hasPlan: Actions = {
    Actions().onCommand[WrappedPlanEvent, Done] {
      case (WrappedPlanEvent(me), ctx, _) =>
        me match {
          case PlanCreated(planDescription) =>
            // do nothing
            ctx.done
          case _ => ctx.thenPersist(me)(evt => ctx.reply(Done))
        }
    }.onReadOnlyCommand[GetPlan.type, SimplePlan] {
      case (GetPlan, ctx, Some(plan)) =>
        ctx.reply(SimplePlan(plan))

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

trait PlanReaderCommand
case class WrappedPlanEvent(planEvent: PlanEvent) extends PlanReaderCommand with ReplyType[Done]


case object GetPlan extends PlanReaderCommand with ReplyType[SimplePlan]





