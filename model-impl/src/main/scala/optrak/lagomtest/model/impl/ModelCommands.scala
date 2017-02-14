package optrak.lagomtest.model.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import optrak.lagomtest.datamodel.Models._
import optrak.lagomtest.datamodel.ModelsJson._
import play.api.libs.json.{Format, Json}
  /**
  * Created by tim on 29/01/17.
  * Copyright Tim Pigden, Hertford UK
  * Defines the commands for the model
  */
object ModelCommands {

  sealed trait ModelCommand extends ReplyType[Done]

  case class CreateModel(modelDescription: ModelDescription) extends ModelCommand

  case class AddOrUpdateProduct(modelId: ModelId, product: Product) extends ModelCommand
  case class UpdateProduct(modelId: ModelId, product: Product) extends ModelCommand
  case class AddProduct(modelId: ModelId, product: Product) extends ModelCommand
  case class RemoveProduct(modelId: ModelId, productId: ProductId) extends ModelCommand

  case class AddOrUpdateSite(modelId: ModelId, site: Site) extends ModelCommand
  case class UpdateSite(modelId: ModelId, site: Site) extends ModelCommand
  case class AddSite(modelId: ModelId, site: Site) extends ModelCommand
  case class RemoveSite(modelId: ModelId, siteId: SiteId) extends ModelCommand

  implicit def formatCreateModel: Format[CreateModel] = Json.format[CreateModel]
  implicit def formatAddOrUpdateProduct: Format[AddOrUpdateProduct] = Json.format[AddOrUpdateProduct]
  implicit def formatUpdateProduct: Format[UpdateProduct] = Json.format[UpdateProduct]
  implicit def formatAddProduct: Format[AddProduct] = Json.format[AddProduct]
  implicit def formatRemoveProduct: Format[RemoveProduct] = Json.format[RemoveProduct]

  implicit def formatAddOrUpdateSite: Format[AddOrUpdateSite] = Json.format[AddOrUpdateSite]
  implicit def formatUpdateSite: Format[UpdateSite] = Json.format[UpdateSite]
  implicit def formatAddSite: Format[AddSite] = Json.format[AddSite]
  implicit def formatRemoveSite: Format[RemoveSite] = Json.format[RemoveSite]


}
