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

  case class Site(id: SiteId, siteCode: String, town: String, address: Option[String], postcode: String)

  type ModelId = UUID

  case class Model(id: ModelId,
                   description: String,
                   products: Map[ProductId, Product] = Map.empty,
                   sites: Map[SiteId, Site] = Map.empty,
                   deleted: Boolean = false)

  object Model {
    def apply(modelDescription: ModelDescription) = new Model(modelDescription.id, modelDescription.description)
  }

  type TenantId = String

  // todo - add active/inactive to test repository querying from uri
  // case class Client(id: ClientId, models: Set[ModelDescription], description: String, active: Boolean)
  case class Tenant(id: TenantId, models: Set[ModelDescription], description: String)

  case class ModelDescription(id: ModelId, description: String)


}
