package optrak.lagomtest.products.impl

import akka.{Done, NotUsed}
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import grizzled.slf4j.Logging
import optrak.lagomtest.datamodel.Models.{Product, ProductId, TenantId}
import optrak.lagomtest.products.api.ProductEvents.{ProductCancelled => ApiProductCancelled, ProductCreated => ApiProductCreated, ProductEvent => ApiProductEvent}
import optrak.lagomtest.products.api._
import optrak.lagomtest.products.impl.ProductEvents.{ProductCreated, ProductEvent}

import scala.concurrent.ExecutionContext

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ProductServiceImpl(persistentEntityRegistry: PersistentEntityRegistry
                         // ,productRepository: ProductRepository
                        )
                        (implicit ec: ExecutionContext)
  extends ProductService with Logging {

  // note because we're runing a multi-tenancy app, the tenantId must be part of the entity id -
  // may have same product codes
  def entityId(tenantId: TenantId, product: ProductId) = s"$tenantId:$product"

  def ref(tenantId: TenantId, id: ProductId) =
      persistentEntityRegistry.refFor[ProductEntity](entityId(tenantId, id))

  def directoryRef(tenantId: TenantId) =
    persistentEntityRegistry.refFor[TenantProductDirectoryEntity](tenantId)


  override def createProduct(tenantId: TenantId, id: ProductId): ServiceCall[ProductCreationData, Done] = ServiceCall { request =>
    logger.debug(s"creating product $id")
    ref(tenantId, id).ask(CreateProduct(tenantId, id, request.size, request.group)).map { res =>
      logger.debug(s"created product $id")
      res
    }
  }
  override def updateSize(tenantId: TenantId, id: ProductId, newSize: Int): ServiceCall[NotUsed, Done] = ServiceCall { request =>
    ref(tenantId, id).ask(UpdateProductSize(tenantId, id, newSize))
  }
  override def updateGroup(tenantId: TenantId, id: ProductId, newGroup: String): ServiceCall[NotUsed, Done] = ServiceCall { request =>
    ref(tenantId, id).ask(UpdateProductGroup(tenantId, id, newGroup))
  }
  override def cancelProduct(tenantId: TenantId, id: ProductId): ServiceCall[NotUsed, Done] = ServiceCall { request =>
    ref(tenantId, id).ask(CancelProduct(tenantId, id))
  }

  override def getProduct(tenantId: TenantId, id: ProductId): ServiceCall[NotUsed, Product] = ServiceCall { request =>
    ref(tenantId, id).ask(GetProduct).map {
      case Some(product) => product
      case None => throw NotFound(s"Product ${ref(tenantId, id)} not found")
    }
  }

  override def getProductsForTenant(tenantId: TenantId): ServiceCall[NotUsed, ProductStatuses] = ServiceCall { _ =>
    directoryRef(tenantId).ask(GetAllProducts)
  }

  override def getLiveProductsForTenant(tenantId: TenantId): ServiceCall[NotUsed, ProductIds] = ServiceCall { _ =>
    directoryRef(tenantId).ask(GetLiveProducts)
  }

  override def productEvents: Topic[ApiProductEvent] = TopicProducer.taggedStreamWithOffset(ProductEvent.Tag.allTags.toList) { (tag, offset) =>
    persistentEntityRegistry.eventStream(tag, offset).map { t =>
      val event = t.event
      ref(event.tenantId, event.productId).ask(GetProduct).flatMap {
        case Some(product) =>
          event match {
            case pc: ProductCreated =>
              logger.debug(s"impl got productCreated $pc")
              directoryRef(event.tenantId).ask(WrappedCreateProduct(event.productId)).map { _ =>
                ApiProductCreated(event.tenantId, product.id)
              }
            case cancelled: ApiProductCancelled =>
              logger.debug(s"imple got productCancelled $cancelled")
              directoryRef(event.tenantId).ask(WrappedCancelProduct(event.productId)).map { _ =>
                ApiProductCancelled(event.tenantId, product.id)
              }
          }
      }.map(x => (x, offset))
    }.mapAsync(1)(identity)

  }
}


