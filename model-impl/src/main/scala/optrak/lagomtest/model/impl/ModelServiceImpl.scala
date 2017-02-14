package optrak.lagomtest.datamodel.impl

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import optrak.lagomtest.datamodel.Models
import optrak.lagomtest.datamodel.Models._
import optrak.lagomtest.model.api.{ModelEvents, ModelService}
import ModelEvents._
import optrak.lagomtest.model.impl.ModelCommands._
import optrak.lagomtest.model.api.ModelEvents.ModelCreated

import scala.concurrent.Future

/**
  * Created by tim on 26/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  * Most methods go straight through to the  model entity.
  */
class ModelServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends ModelService {

  /**
    * most messages go straight through to the entity
    */
  private def toModel[Request](modelId: ModelId, f: (ModelId, Request) => ModelCommand): ServiceCall[Request, Done] =
    ServiceCall { request =>
      val ref = persistentEntityRegistry.refFor[ModelEntity](modelId.toString)
      ref.ask(f(modelId, request))
    }

  override def createModel(modelId: ModelId): ServiceCall[String, Done] =
    toModel(modelId, ( (mid, req) => CreateModel(ModelDescription(mid, req))))

  override def addProduct(modelId: ModelId): ServiceCall[Models.Product, Done] =
    toModel(modelId, AddProduct)


  override def updateProduct(modelId: ModelId): ServiceCall[Models.Product, Done] =
    toModel(modelId, UpdateProduct)

  override def addOrUpdateProduct(modelId: ModelId): ServiceCall[Models.Product, Done] =
    toModel(modelId, AddOrUpdateProduct)

  override def removeProduct(modelId: ModelId): ServiceCall[ProductId, Done] =
    toModel(modelId, RemoveProduct)

  override def addSite(modelId: ModelId): ServiceCall[Models.Site, Done] =
    toModel(modelId, AddSite)

  override def updateSite(modelId: ModelId): ServiceCall[Models.Site, Done] =
    toModel(modelId, UpdateSite)

  override def addOrUpdateSite(modelId: ModelId): ServiceCall[Models.Site, Done] =
    toModel(modelId, AddOrUpdateSite)

  override def removeSite(modelId: ModelId): ServiceCall[SiteId, Done] =
    toModel(modelId, RemoveSite)


  override def modelEvents(): Topic[ModelEvents.ModelEvent] = TopicProducer.taggedStreamWithOffset(ModelEvent.Tag.allTags.toList) { (tag, offset) =>
    persistentEntityRegistry.eventStream(tag, offset).map(t => (t.event, offset))
  }

  override def product(modelId: ModelId, productId: ProductId): ServiceCall[NotUsed, Models.Product] = ???

  override def products(modelId: ModelId): ServiceCall[NotUsed, Seq[Models.Product]] = ???

  override def site(modelId: ModelId, siteId: SiteId): ServiceCall[NotUsed, Models.Site] = ???

  override def sites(modelId: ModelId): ServiceCall[NotUsed, Seq[Models.Site]] = ???
}
