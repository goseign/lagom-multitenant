package optrak.lagomtest.data

import java.util.UUID

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  */
object Data {

  type ProductId = String

  case class Product(id: ProductId, size: Int, group: String, cancelled: Boolean)

  type SiteId = String

  case class Site(id: SiteId, postcode: String)

  type OrderId = String

  // used for entity, uses references to site and product
  case class Order(id: OrderId, site: SiteId, product: ProductId, quantity: Int)

  type VehicleId = String

  // for entity
  case class Vehicle(id: VehicleId, capacity: Int)

  type TripId = String
  // used in plan
  case class Trip(vehicle: VehicleId, orders: List[OrderId])

  type PlanId = UUID

  /** Plan corresponds to a micro version of our vehicle routing model.
    * In DDD terms it is a root entity and it contains products, sites and orders as Value objects -
    * that is these are just copies of the real thing. They may have (probably will have) been derived from
    * the respective entitities but they may also be modified locally without changing the underlying order or whatever.
    * For example "what would happen if the orders would all be 10% bigger?" is a valid vrp query.
    * Multiple models exist per tenant and they contain overlapping data.
    */
  trait Plan {
    def id: PlanId
    def description: String
    def products: Set[Product]
    def sites: Set[Site]
    def orders: Set[Order]
    def vehicles: Set[Vehicle]
    def trips: Set[Trip]
  }

  type TenantId = String

  case class Tenant(id: TenantId, models: Set[PlanDescription], description: String)

  case class PlanDescription(id: PlanId, description: String)


}
