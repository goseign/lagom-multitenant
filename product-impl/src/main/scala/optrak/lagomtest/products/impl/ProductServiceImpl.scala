package optrak.lagomtest.products.impl

import akka.{Done, NotUsed}
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import optrak.lagomtest.datamodel.Models.{Product, ProductId, TenantId}
import optrak.lagomtest.products.api.{ProductCreationData, ProductService, ProductStatus}

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ProductServiceImpl(persistentEntityRegistry: PersistentEntityRegistry, productRepository: ProductRepository) extends ProductService {

  // note because we're runing a multi-tenancy app, the tenantId must be part of the entity id -
  // may have same product codes
  def entityId(tenantId: TenantId, product: ProductId) = s"$tenantId:$product"

  def ref(tenantId: TenantId, id: ProductId) =
      persistentEntityRegistry.refFor[ProductEntity](entityId(tenantId, id))

  override def createProduct(tenantId: TenantId, id: ProductId): ServiceCall[ProductCreationData, Done] = ServiceCall { request =>
    ref(tenantId, id).ask(CreateProduct(tenantId, id, request.size, request.group))
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

  override def getProduct(tenantId: TenantId, id: ProductId): ServiceCall[NotUsed, Product] = ???

  override def getProductsForTenant(tenant: TenantId): ServiceCall[NotUsed, List[ProductStatus]] = ???

  override def getLiveProductsForTenant(tenantId: TenantId): ServiceCall[NotUsed, List[ProductId]] = ???

}

