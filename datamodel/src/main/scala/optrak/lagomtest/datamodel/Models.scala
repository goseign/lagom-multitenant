package optrak.lagomtest.datamodel

import java.util.UUID

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object Models {

  type ProductId = UUID

  case class Product(id: ProductId, size: Int, group: String)

  type SiteId = UUID

  case class Site(id: SiteId, siteCode: String, town: String, address: Option[String], postcode: String)

  type ModelId = UUID

  case class Model(id: ModelId, products: Set[Product], sites: Set[Site], description: String, deleted: Boolean)

  type ClientId = String

  // todo - add active/inactive to test repository querying from uri
  // case class Client(id: ClientId, models: Set[ModelDescription], description: String, active: Boolean)
  case class Client(id: ClientId, models: Set[ModelDescription], description: String)

  case class ModelDescription(id: ModelId, description: String)


}
