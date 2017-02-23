package optrak.lagomtest.plan.api

import com.lightbend.lagom.scaladsl.api.transport.{TransportErrorCode, TransportException}
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer
import optrak.lagomtest.data.Data._
import optrak.lagomtest.plan.api.PlanService.{CheckedResult, ErrorMessage}
import play.api.libs.json._
import optrak.lagomtest.data.DataJson._

/**
  * Created by tim on 30/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object PlanEntityErrors {


  case class PlanAlreadyExistsError(planDescription: PlanDescription) extends ErrorMessage {
    override def toString = s"plan $planDescription already exists"

    def planId = planDescription.id
  }

  object PlanAlreadyExistsError {
    implicit val format = Json.format[PlanAlreadyExistsError]
  }

  case class ProductAlreadyDefinedError(productId: ProductId, planId: PlanId) extends ErrorMessage {
    override def toString = s"product $productId already defined in plan $planId"
  }

  object ProductAlreadyDefinedError {
    implicit val format = Json.format[ProductNotDefinedError]
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

  // this is clumsy because i'm not familiar with play json best practice
  implicit def readsChecked[T <: ErrorMessage](implicit emSerializer: JsonSerializer[T] ): Reads[CheckedResult[T]] = new Reads[CheckedResult[T]] {
    override def reads(json: JsValue): JsResult[CheckedResult[T]] = {
      val labelX = (__ \ "result").read[String]
      for {
        label <- labelX.reads(json)
        res <- label match {
          case "done" => JsSuccess(CheckedResult.empty[T])
          // case "error" => emSerializer.format.reads(json).map(Some(_))
        }
      } yield res
    }
  }

  implicit def writesChecked[T <: ErrorMessage](implicit emSerializer: JsonSerializer[T]): Writes[CheckedResult[T]] = ???


}
