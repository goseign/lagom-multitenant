package optrak.lagomtest.plan.api

import optrak.lagomtest.data.Data._
import play.api.libs.json._
import optrak.lagomtest.data.DataJson._

/**
  * Created by tim on 22/02/17.
  * Copyright Tim Pigden, Hertford UK
  */
case class PlanImpl(id: PlanId,
  description: String,
  productsM: Map[ProductId, Product],
  sitesM: Map[SiteId, Site],
  ordersM: Map[OrderId, Order],
  vehiclesM: Map[VehicleId, Vehicle],
  tripsM: Map[TripId, Trip])
  extends Plan {
  override def products: Set[Product] = productsM.values.toSet

  override def sites: Set[Site] = sitesM.values.toSet

  override def orders: Set[Order] = ordersM.values.toSet

  override def vehicles: Set[Vehicle] = vehiclesM.values.toSet

  override def trips: Set[Trip] = tripsM.values.toSet
}

object PlanImpl {
  def apply(planDescription: PlanDescription,
            productsM: Map[ProductId, Product] = Map.empty,
            sitesM: Map[SiteId, Site] = Map.empty,
            ordersM: Map[OrderId, Order] = Map.empty,
            vehiclesM: Map[VehicleId, Vehicle] = Map.empty,
            tripsM: Map[TripId, Trip] = Map.empty): PlanImpl =
    new PlanImpl (planDescription.id, planDescription.description, productsM, sitesM, ordersM, vehiclesM, tripsM)

  implicit val format = Json.format[PlanImpl]
}
