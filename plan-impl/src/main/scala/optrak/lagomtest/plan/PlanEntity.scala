package optrak.lagomtest.plan

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import optrak.lagomtest.data.Data._
import optrak.lagomtest.plan.api.PlanEvents._
import optrak.lagomtest.plan.api.PlanEntityErrors._
import optrak.lagomtest.plan.PlanCommands._
import optrak.lagomtest.plan.api.PlanEntityErrors.PlanAlreadyExistsError
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
        ctx.commandFailed(PlanAlreadyExistsError(planDescription))
        ctx.done
    }.onCommand[UpdateProduct, Done] {
      case (UpdateProduct(planId, product), ctx, Some(plan)) =>
        if (plan.productsM.contains(product.id))
          ctx.thenPersist(ProductUpdated(planId, product))(evt => ctx.reply(Done))
        else
          throw new ProductNotDefinedError(product.id, plan.id)
    }.onCommand[AddProduct, Done] {
      case (AddProduct(planId, product), ctx, Some(plan)) =>
        if (!plan.productsM.contains(product.id))
          ctx.thenPersist(ProductUpdated(planId, product))(evt => ctx.reply(Done))
        else
          throw new ProductAlreadyDefinedError(product.id, plan.id)
    }.onCommand[RemoveProduct, Done] {
      case (RemoveProduct(planId, productId), ctx, Some(plan)) =>
        val referencingOrders = plan.orders.filter(_.product == productId)
        if (referencingOrders.size > 0)
          throw new ProductReferencedByOrdersError(productId, referencingOrders.map(_.id).toList, plan.id)
        else
          ctx.thenPersist(ProductRemoved(planId, productId))(evt => ctx.reply(Done))
    }.onCommand[UpdateSite, Done] {
      case (UpdateSite(planId, site), ctx, Some(plan)) =>
        if (plan.sitesM.contains(site.id))
          ctx.thenPersist(SiteUpdated(planId, site))(evt => ctx.reply(Done))
        else
          throw new SiteNotDefinedError(site.id, plan.id)
    }.onCommand[AddSite, Done] {
      case (AddSite(planId, site), ctx, Some(plan)) =>
        if (!plan.sitesM.contains(site.id))
          ctx.thenPersist(SiteUpdated(planId, site))(evt => ctx.reply(Done))
        else
          throw new SiteAlreadyDefinedError(site.id, plan.id)
    }.onCommand[RemoveSite, Done] {
      case (RemoveSite(planId, siteId), ctx, Some(plan)) =>
        val referencingOrders = plan.orders.filter(_.site == siteId)
        if (referencingOrders.size > 0)
          throw new SiteReferencedByOrdersError(siteId, referencingOrders.map(_.id).toList, plan.id)
        else
        ctx.thenPersist(SiteRemoved(planId, siteId))(evt => ctx.reply(Done))
    }.onCommand[UpdateVehicle, Done] {
      case (UpdateVehicle(planId, vehicle), ctx, Some(plan)) =>
        if (plan.vehiclesM.contains(vehicle.id))
          ctx.thenPersist(VehicleUpdated(planId, vehicle))(evt => ctx.reply(Done))
        else
          throw new VehicleNotDefinedError(vehicle.id, plan.id)
    }.onCommand[AddVehicle, Done] {
      case (AddVehicle(planId, vehicle), ctx, Some(plan)) =>
        if (!plan.vehiclesM.contains(vehicle.id))
          ctx.thenPersist(VehicleUpdated(planId, vehicle))(evt => ctx.reply(Done))
        else
          throw new VehicleAlreadyDefinedError(vehicle.id, plan.id)
    }.onCommand[RemoveVehicle, Done] {
      case (RemoveVehicle(planId, vehicleId), ctx, Some(plan)) =>
        ctx.thenPersist(VehicleRemoved(planId, vehicleId))(evt => ctx.reply(Done))
    }.onCommand[UpdateOrder, Done] {
      case (UpdateOrder(planId, order), ctx, Some(plan)) =>
        if (plan.ordersM.contains(order.id))
          ctx.thenPersist(OrderUpdated(planId, order))(evt => ctx.reply(Done))
        else
          throw new OrderNotDefinedError(order.id, plan.id)
    }.onCommand[AddOrder, Done] {
      case (AddOrder(planId, order), ctx, Some(plan)) =>
        if (!plan.productsM.contains(order.product))
          throw new ProductNotDefinedError(order.product, plan.id)
        if (!plan.sitesM.contains(order.site))
          throw new SiteNotDefinedError(order.site, plan.id)
        else if (plan.ordersM.contains(order.id))
          throw new OrderAlreadyDefinedError(order.id, plan.id)
        else
          ctx.thenPersist(OrderUpdated(planId, order))(evt => ctx.reply(Done))
    }.onCommand[RemoveOrder, Done] {
      case (RemoveOrder(planId, orderId), ctx, Some(plan)) =>
        ctx.thenPersist(OrderRemoved(planId, orderId))(evt => ctx.reply(Done))
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
    }.onEvent {
      case (VehicleUpdated(planId, newVehicle), Some(plan)) =>
        Some(plan.copy(vehiclesM = plan.vehiclesM + (newVehicle.id -> newVehicle)))
    }.onEvent {
      case (VehicleRemoved(planId, vehicleId), Some(plan)) =>
        Some(plan.copy(vehiclesM = plan.vehiclesM - vehicleId))
    }.onEvent {
      case (OrderUpdated(planId, newOrder), Some(plan)) =>
        Some(plan.copy(ordersM = plan.ordersM + (newOrder.id -> newOrder)))
    }.onEvent {
      case (OrderRemoved(planId, orderId), Some(plan)) =>
        Some(plan.copy(ordersM = plan.ordersM - orderId))
    }
    
    
  }
}





