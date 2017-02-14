package optrak.lagomtest.modelreader.impl

import akka.Done
import com.fasterxml.jackson.annotation.JsonFormat
import com.lightbend.lagom.scaladsl.api.transport.{TransportErrorCode, TransportException}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import optrak.lagomtest.datamodel.Models._
import optrak.lagomtest.model.api.ModelEvents
import optrak.lagomtest.model.api.ModelEvents._
import optrak.lagomtest.utils.JsonFormats
import play.api.libs.json.{Format, Json, Reads, Writes}

import scala.concurrent.Future

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  * NB this uses a one-to-one map of event to internal event. Why not?
  */


class ModelReaderEntity extends PersistentEntity {

  override type Command = ModelReaderCommand
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
      .onCommand[WrappedModelEvent, Done] {
      case (WrappedModelEvent(ModelCreated(modelDescription)), ctx, _) =>
        ctx.thenPersist(ModelCreated(modelDescription))(evt =>
          ctx.reply(Done))
    }.onEvent{
      case (ModelCreated(modelDescription), _) =>
        Some(Model(modelDescription))
    }
  }

  def hasModel: Actions = {
    Actions().onCommand[WrappedModelEvent, Done] {
      case (WrappedModelEvent(me), ctx, _) =>
        me match {
          case ModelCreated(modelDescription) =>
            // do nothing
            ctx.done
          case _ => ctx.thenPersist(me)(evt => ctx.reply(Done))
        }
    }.onReadOnlyCommand[GetModel.type, Model] {
      case (GetModel, ctx, Some(model)) =>
        ctx.reply(model)

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

trait ModelReaderCommand
case class WrappedModelEvent(modelEvent: ModelEvent) extends ModelReaderCommand with ReplyType[Done]

object WrappedModelEvent {
  implicit val format : Format[WrappedModelEvent] = Format[WrappedModelEvent](
    Reads[WrappedModelEvent] { js =>
      ModelEvents.reads.reads(js).map { ev => WrappedModelEvent(ev)}
    } ,
    Writes { o =>
      ModelEvents.writes.writes(o.modelEvent)
    }
  )
}


case object GetModel extends ModelReaderCommand with ReplyType[Model] {
  implicit def format: Format[GetModel.type] = JsonFormats.singletonFormat(GetModel)
}





