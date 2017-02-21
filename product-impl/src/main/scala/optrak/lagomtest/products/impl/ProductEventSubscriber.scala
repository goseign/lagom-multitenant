package optrak.lagomtest.products.impl

import akka.Done
import akka.stream.scaladsl.Flow
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import optrak.lagomtest.datamodel.Models.TenantId
import optrak.lagomtest.products.api.ProductEvents.{ProductEvent => ApiProductEvent}
import optrak.lagomtest.products.api.ProductService
import optrak.lagomtest.products.api.ProductEvents.{ProductCancelled => ApiProductCancelled, ProductCreated => ApiProductCreated, ProductEvent => ApiProductEvent}

import scala.concurrent.Future

/**
  * Created by tim on 18/02/17.
  * Copyright Tim Pigden, Hertford UK
  *
class ProductEventSubscriber(persistentEntityRegistry: PersistentEntityRegistry, productService: ProductService) {

  def ref(tenantId: TenantId) =
    persistentEntityRegistry.refFor[TenantProductDirectoryEntity](tenantId)

  productService.productEvents.subscribe.atLeastOnce(Flow[ApiProductEvent].mapAsync(1) {

    case evt: ApiProductCreated =>
      ref(evt.tenantId).ask(WrappedCreateProduct(evt.productId))
    case evt: ApiProductCancelled =>
      ref(evt.tenantId).ask(WrappedCancelProduct(evt.productId))

    case other =>
      Future.successful(Done)

  })

}
*/