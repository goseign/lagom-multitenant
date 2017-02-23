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

  case class UpdateProduct(planId: PlanId, product: Product) extends PlanCommand
  case class AddProduct(planId: PlanId, product: Product) extends PlanCommand
  case class RemoveProduct(planId: PlanId, productId: ProductId) extends PlanCommand

    case class UpdateSite(planId: PlanId, site: Site) extends PlanCommand
    case class AddSite(planId: PlanId, site: Site) extends PlanCommand
    case class RemoveSite(planId: PlanId, siteId: SiteId) extends PlanCommand

    case class UpdateVehicle(planId: PlanId, vehicle: Vehicle) extends PlanCommand
    case class AddVehicle(planId: PlanId, vehicle: Vehicle) extends PlanCommand
    case class RemoveVehicle(planId: PlanId, vehicleId: VehicleId) extends PlanCommand

    case class UpdateOrder(planId: PlanId, order: Order) extends PlanCommand
    case class AddOrder(planId: PlanId, order: Order) extends PlanCommand
    case class RemoveOrder(planId: PlanId, orderId: OrderId) extends PlanCommand

    implicit def formatCreatePlan: Format[CreatePlan] = Json.format[CreatePlan]


  implicit def formatUpdateProduct: Format[UpdateProduct] = Json.format[UpdateProduct]
  implicit def formatAddProduct: Format[AddProduct] = Json.format[AddProduct]
  implicit def formatRemoveProduct: Format[RemoveProduct] = Json.format[RemoveProduct]

  implicit def formatUpdateSite: Format[UpdateSite] = Json.format[UpdateSite]
  implicit def formatAddSite: Format[AddSite] = Json.format[AddSite]
  implicit def formatRemoveSite: Format[RemoveSite] = Json.format[RemoveSite]

    implicit def formatUpdateVehicle: Format[UpdateVehicle] = Json.format[UpdateVehicle]
    implicit def formatAddVehicle: Format[AddVehicle] = Json.format[AddVehicle]
    implicit def formatRemoveVehicle: Format[RemoveVehicle] = Json.format[RemoveVehicle]

    implicit def formatUpdateOrder: Format[UpdateOrder] = Json.format[UpdateOrder]
    implicit def formatAddOrder: Format[AddOrder] = Json.format[AddOrder]
    implicit def formatRemoveOrder: Format[RemoveOrder] = Json.format[RemoveOrder]

}
