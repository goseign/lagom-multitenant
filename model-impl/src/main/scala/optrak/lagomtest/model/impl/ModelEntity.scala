package optrak.lagomtest.datamodel.impl

import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.{TransportErrorCode, TransportException}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import optrak.lagomtest.datamodel.Models._
import optrak.lagomtest.model.impl.ModelCommands._
import optrak.lagomtest.model.api.ModelEvents._
import play.api.libs.json.{Format, Json}
import optrak.lagomtest.model.impl.ModelEntityExceptions._
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */

class ModelEntity extends PersistentEntity {


  override type Command = ModelCommand
  override type Event = ModelEvent
  override type State = Option[Model]

  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState = None

  /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case None => noModelYet
    case Some(model) => hasModel
  }

  def noModelYet: Actions = {
    Actions()
    .onCommand[CreateModel, Done] {
      case (CreateModel(modelDescription), ctx, _) =>
        ctx.thenPersist(ModelCreated(modelDescription))(evt =>
          ctx.reply(Done))
    }.onEvent{
      case (ModelCreated(modelDescription), _) =>
        Some(Model(modelDescription))
    }
  }

  def hasModel: Actions = {
    Actions().onCommand[CreateModel, Done] {
      case (CreateModel(modelDescription), ctx, _) =>
        throw new ModelAlreadyExistsException(modelDescription)
    }.onCommand[AddOrUpdateProduct, Done] {
      case (AddOrUpdateProduct(modelId, product), ctx, state) =>
        ctx.thenPersist(ProductUpdated(modelId, product))(evt => ctx.reply(Done))
    }.onCommand[UpdateProduct, Done] {
      case (UpdateProduct(modelId, product), ctx, Some(model)) =>
        if (model.products.contains(product.id))
          ctx.thenPersist(ProductUpdated(modelId, product))(evt => ctx.reply(Done))
        else
          throw new ProductNotDefinedException(product.id, model.id)
    }.onCommand[AddProduct, Done] {
      case (AddProduct(modelId, product), ctx, Some(model)) =>
        if (!model.products.contains(product.id))
          ctx.thenPersist(ProductUpdated(modelId, product))(evt => ctx.reply(Done))
        else
          throw new ProductAlreadyDefinedException(product.id, model.id)
    }.onCommand[RemoveProduct, Done] {
      case (RemoveProduct(modelId, productId), ctx, Some(model)) =>
        ctx.thenPersist(ProductRemoved(modelId, productId))(evt => ctx.reply(Done))
    }.onCommand[AddOrUpdateSite, Done] {
      case (AddOrUpdateSite(modelId, site), ctx, state) =>
        ctx.thenPersist(SiteUpdated(modelId, site))(evt => ctx.reply(Done))
    }.onCommand[UpdateSite, Done] {
      case (UpdateSite(modelId, site), ctx, Some(model)) =>
        if (model.sites.contains(site.id))
          ctx.thenPersist(SiteUpdated(modelId, site))(evt => ctx.reply(Done))
        else
          throw new SiteNotDefinedException(site.id, model.id)
    }.onCommand[AddSite, Done] {
      case (AddSite(modelId, site), ctx, Some(model)) =>
        if (!model.sites.contains(site.id))
          ctx.thenPersist(SiteUpdated(modelId, site))(evt => ctx.reply(Done))
        else
          throw new SiteAlreadyDefinedException(site.id, model.id)
    }.onCommand[RemoveSite, Done] {
      case (RemoveSite(modelId, siteId), ctx, Some(model)) =>
        ctx.thenPersist(SiteRemoved(modelId, siteId))(evt => ctx.reply(Done))
    }.onEvent {
      case (ProductUpdated(modelId, newProduct), Some(model)) =>
        Some(model.copy(products = model.products + (newProduct.id -> newProduct)))
    }.onEvent {
      case (ProductRemoved(modelId, productId), Some(model)) =>
        Some(model.copy(products = model.products - productId))
    }.onEvent {
      case (SiteUpdated(modelId, newSite), Some(model)) =>
        Some(model.copy(sites = model.sites + (newSite.id -> newSite)))
    }.onEvent {
      case (SiteRemoved(modelId, siteId), Some(model)) =>
        Some(model.copy(sites = model.sites - siteId))
    }
  }
}





