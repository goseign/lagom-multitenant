package optrak.lagomtest.plan.api

import com.lightbend.lagom.scaladsl.api.transport.{TransportErrorCode, TransportException}
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer
import optrak.lagomtest.data.Data._
import optrak.lagomtest.plan.api.PlanService.{CheckedResult, ErrorMessage}
import play.api.libs.json._

/**
  * Created by tim on 30/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object PlanEntityErrors {


  case class PlanAlreadyExistsError(planDescription: PlanDescription) extends ErrorMessage {
    override def toString = s"plan $planDescription already exists"

    def planId = planDescription.id
  }

  case class ProductAlreadyDefinedError(productId: ProductId, planId: PlanId) extends ErrorMessage {
    override def toString = s"product $productId already defined in plan $planId"
  }

  case class ProductNotDefinedError(productId: ProductId, planId: PlanId) extends ErrorMessage {
    override def toString = s"product $productId not defined in plan $planId"
  }

  case class SiteAlreadyDefinedError(siteId: SiteId, planId: PlanId) extends ErrorMessage {
    override def toString = s"site $siteId already defined in plan $planId"
  }

  case class SiteNotDefinedError(siteId: SiteId, planId: PlanId) extends ErrorMessage {
    override def toString = s"site $siteId not defined in plan $planId"
  }

  case class OrderAlreadyDefinedError(orderId: OrderId, planId: PlanId) extends ErrorMessage {
    override def toString = s"order $orderId already defined in plan $planId"
  }

  case class OrderNotDefinedError(orderId: OrderId, planId: PlanId) extends ErrorMessage {
    override def toString = s"order $orderId not defined in plan $planId"
  }

  case class VehicleAlreadyDefinedError(vehicleId: VehicleId, planId: PlanId) extends ErrorMessage {
    override def toString = s"vehicle $vehicleId already defined in plan $planId"
  }

  case class VehicleNotDefinedError(vehicleId: VehicleId, planId: PlanId) extends ErrorMessage {
    override def toString = s"vehicle $vehicleId not defined in plan $planId"
  }

  case class ProductReferencedByOrdersError(productId: ProductId, orderIds: List[OrderId], planId: PlanId) extends ErrorMessage {
    override def toString = s"product $productId referenced by orders $orderIds plan $planId"
  }

  case class SiteReferencedByOrdersError(siteId: SiteId, orderIds: List[OrderId], planId: PlanId) extends ErrorMessage {
    override def toString = s"site $siteId referenced by orders $orderIds plan $planId"
  }


}
