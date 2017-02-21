package optrak.lagomtest.datamodel

import java.util.UUID

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  */
object Models {

  type ProductId = String

  case class Product(id: ProductId, size: Int, group: String, cancelled: Boolean)

  type SiteId = String

  case class Site(id: SiteId, postcode: String)

  type OrderId = String

  // used only in the model. Order as value object with direct links to data
  case class OrderValue(id: OrderId, site: Site, product: Product, quantity: Int)

  // used for entity, uses references to site and product
  case class Order(id: OrderId, site: SiteId, product: ProductId, quantity: Int)

  type ModelId = UUID

  /** Model corresponds to a micro version of our vehicle routing model.
    * In DDD terms it is a root entity and it contains products, sites and orders as Value objects -
    * that is these are just copies of the real thing. They may have (probably will have) been derived from
    * the respective entitities but they may also be modified locally without changing the underlying order or whatever.
    * For example "what would happen if the orders would all be 10% bigger?" is a valid vrp query.
    * Multiple models exist per tenant and they contain overlapping data.
    *
    * @param id
    * @param description
    * @param products
    * @param sites
    * @param orders
    * @param deleted
    */
  case class Model(id: ModelId,
                   description: String,
                   products: Map[ProductId, Product] = Map.empty,
                   sites: Map[SiteId, Site] = Map.empty,
                   orders: Map[OrderId, OrderValue] = Map.empty,
                   deleted: Boolean = false)

  object Model {
    def apply(modelDescription: ModelDescription) = new Model(modelDescription.id, modelDescription.description)
  }

  type TenantId = String

  case class Tenant(id: TenantId, models: Set[ModelDescription], description: String)

  case class ModelDescription(id: ModelId, description: String)


}
