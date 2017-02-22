package optrak.lagomtest.plan

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import optrak.lagomtest.data.Data._
import optrak.lagomtest.data.DataJson._
import play.api.libs.json.{Format, Json}
  /**
  * Created by tim on 29/01/17.
  * Copyright Tim Pigden, Hertford UK
  * Defines the commands for the plan
  */
object PlanCommands {

  sealed trait PlanCommand extends ReplyType[Done]

  case class CreatePlan(planDescription: PlanDescription) extends PlanCommand

  case class AddOrUpdateProduct(planId: PlanId, product: Product) extends PlanCommand
  case class UpdateProduct(planId: PlanId, product: Product) extends PlanCommand
  case class AddProduct(planId: PlanId, product: Product) extends PlanCommand
  case class RemoveProduct(planId: PlanId, productId: ProductId) extends PlanCommand

  case class AddOrUpdateSite(planId: PlanId, site: Site) extends PlanCommand
  case class UpdateSite(planId: PlanId, site: Site) extends PlanCommand
  case class AddSite(planId: PlanId, site: Site) extends PlanCommand
  case class RemoveSite(planId: PlanId, siteId: SiteId) extends PlanCommand

  implicit def formatCreatePlan: Format[CreatePlan] = Json.format[CreatePlan]
  implicit def formatAddOrUpdateProduct: Format[AddOrUpdateProduct] = Json.format[AddOrUpdateProduct]
  implicit def formatUpdateProduct: Format[UpdateProduct] = Json.format[UpdateProduct]
  implicit def formatAddProduct: Format[AddProduct] = Json.format[AddProduct]
  implicit def formatRemoveProduct: Format[RemoveProduct] = Json.format[RemoveProduct]

  implicit def formatAddOrUpdateSite: Format[AddOrUpdateSite] = Json.format[AddOrUpdateSite]
  implicit def formatUpdateSite: Format[UpdateSite] = Json.format[UpdateSite]
  implicit def formatAddSite: Format[AddSite] = Json.format[AddSite]
  implicit def formatRemoveSite: Format[RemoveSite] = Json.format[RemoveSite]


}
