package optrak.lagomtest.model

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object Models {

  type ProductId = String

  case class Product(id: ProductId, size: Int, group: String)

  type SiteId = String

  case class Site(id: SiteId, town: String, address: Option[String], postcode: String)

  type ModelId = String

  case class Model(id: ModelId, products: Set[Product], sites: Set[Site], description: String, deleted: Boolean)

  type ClientId = String

  case class Client(id: ClientId, models: Set[ModelDescription], description: String)

  case class ModelDescription(id: ModelId, description: String)


}
