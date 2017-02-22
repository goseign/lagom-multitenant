package optrak.lagomtest.plan

import com.lightbend.lagom.scaladsl.api.transport.{TransportErrorCode, TransportException}
import optrak.lagomtest.data.Data.{PlanDescription, PlanId, ProductId, SiteId}

/**
  * Created by tim on 30/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object PlanEntityExceptions {
  case class PlanAlreadyExistsException(planDescription: PlanDescription) extends TransportException(TransportErrorCode.UnsupportedData, s"plan $planDescription already exists")
  case class ProductAlreadyDefinedException(productId: ProductId, planId: PlanId) extends TransportException(TransportErrorCode.UnsupportedData, s"product $productId already defined in plan $planId")
  case class ProductNotDefinedException(productId: ProductId, planId: PlanId) extends TransportException(TransportErrorCode.UnsupportedData, s"product $productId nod defined in plan $planId")
  case class SiteAlreadyDefinedException(siteId: SiteId, planId: PlanId) extends TransportException(TransportErrorCode.UnsupportedData, s"site $siteId already defined in plan $planId")
  case class SiteNotDefinedException(siteId: SiteId, planId: PlanId) extends TransportException(TransportErrorCode.UnsupportedData, s"site $siteId nod defined in plan $planId")

}
