package optrak.lagomtest.datamodel.impl

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import optrak.lagomtest.datamodel.Models
import optrak.lagomtest.datamodel.Models.{ClientId, ModelId, ProductId, SiteId}

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ModelServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends ModelService {
  override def createClient(clientId: ClientId): ServiceCall[CreateClient, Done] = ???

  override def createModel(clientId: ClientId): ServiceCall[CreateModel, ModelCreated] = ???

  override def removeModel(modelId: ModelId): Unit = ???

  override def addProduct(modelId: ModelId): ServiceCall[Models.Product, Done] = ???

  override def updateProduct(modelId: ModelId): ServiceCall[Models.Product, Done] = ???

  override def addOrUpdateProduct(modelId: ModelId): ServiceCall[Models.Product, Done] = ???

  override def removeProduct(modelId: ModelId): ServiceCall[ProductId, Done] = ???

  override def addSite(modelId: ModelId): ServiceCall[Models.Site, Done] = ???

  override def updateSite(modelId: ModelId): ServiceCall[Models.Site, Done] = ???

  override def addOrUpdateSite(modelId: ModelId): ServiceCall[Models.Site, Done] = ???

  override def removeSite(modelId: ModelId): ServiceCall[SiteId, Done] = ???

  override def product(modelId: ModelId, productId: ProductId): ServiceCall[NotUsed, Models.Product] = ???

  override def products(modelId: ModelId): ServiceCall[NotUsed, Seq[Models.Product]] = ???

  override def site(modelId: ModelId, siteId: SiteId): ServiceCall[NotUsed, Models.Site] = ???

  override def sites(modelId: ModelId): ServiceCall[NotUsed, Seq[Models.Site]] = ???
}
