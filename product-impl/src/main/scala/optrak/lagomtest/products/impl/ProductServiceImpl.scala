package optrak.lagomtest.products.impl

import akka.{Done, NotUsed}
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import optrak.lagom.products.api.{CreateProductData, ProductService}
import optrak.lagomtest.client.api
import optrak.lagomtest.datamodel.Models
import optrak.lagomtest.datamodel.Models.{ClientId, Product, ProductId}

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ProductServiceImpl(persistentEntityRegistry: PersistentEntityRegistry, clientRepository: ProductRepository) extends ProductService {

  // note because we're runing a multi-tenancy app, the clientId must be part of the entity id - clients
  // may have same product codes
  def entityId(client: ClientId, product: ProductId) = s"$client:$product"

  def ref(client: String, id: String) =
      persistentEntityRegistry.refFor[ProductEntity](entityId(client, id))

  override def createProduct(client: String, id: String): ServiceCall[CreateProductData, Done] = ServiceCall { request =>
    ref(client, id).ask(CreateProduct(client, id, request.size, request.group))
  }
  override def updateSize(client: String, id: String, newSize: Int): ServiceCall[NotUsed, Done] = ServiceCall { request =>
    ref(client, id).ask(UpdateProductSize(client, id, newSize))
  }
  override def updateGroup(client: String, id: String, newGroup: String): ServiceCall[NotUsed, Done] = ServiceCall { request =>
    ref(client, id).ask(UpdateProductGroup(client, id, newGroup))
  }
  override def cancelProduct(client: String, id: String): ServiceCall[NotUsed, Done] = ServiceCall { request =>
    ref(client, id).ask(CancelProduct(client, id))
  }

  override def getProduct(client: String, id: String): ServiceCall[NotUsed, Product] = ???

}

