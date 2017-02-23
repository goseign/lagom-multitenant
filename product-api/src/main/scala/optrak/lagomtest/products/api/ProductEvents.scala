package optrak.lagomtest.products.api

import com.lightbend.lagom.scaladsl.api.broker.Topic
import play.api.libs.json._
import optrak.lagomtest.data.Data._

/**
* Created by tim on 22/01/17.
* Copyright Tim Pigden, Hertford UK
  * Note this is different from  oiptrak.lagomtest.products.impl.ProductEvents in the way the events are defined
*/
object ProductEvents {

  sealed trait ProductEvent {
    def tenantId: TenantId

    def productId: ProductId
  }

  case class ProductCreated(tenantId: TenantId, productId: ProductId) extends ProductEvent

  // unlike the product entity itself, we don't care how it's been updated, we just want the new version
  case class ProductCancelled(tenantId: TenantId, productId: ProductId) extends ProductEvent

}

