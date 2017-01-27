package optrak.lagomtest.model.impl

import java.util.UUID

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import optrak.lagomtest.model.Models._
import optrak.lagomtest.model.api._
import optrak.lagomtest.model.impl.ClientEvents.{ClientCreated, ClientEvent, ModelCreated, ModelRemoved}
import play.api.libs.json.{Format, Json}

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ClientEntity extends PersistentEntity {

  override type Command = ClientCommand
  override type Event = ClientEvent
  override type State = Option[Client]

  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState = None

  /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case None => noClientYet
    case Some(_) => hasClient
  }

  def noClientYet: Actions = {
    Actions()
      .onCommand[CreateClient, Done] {
      case (CreateClient(id: ClientId, description: String), ctx, _) =>
        ctx.thenPersist(ClientCreated(id, description))(evt =>
          ctx.reply(Done)
        )
    }
      .onEvent {
        case (ClientCreated(id, description), _) =>
          Some(Client(id, Set.empty, description))
      }
  }

  def hasClient: Actions = {
    Actions()
      .onCommand[CreateModel, Done] {
      // Command handler for the ChangeClient command
      case (CreateModel(description), ctx, _) =>
        ctx.thenPersist(ModelCreated(UUID.randomUUID().toString, description))(_ =>
          ctx.reply(Done))
    }.onCommand[RemoveModel.type, Done] {
      case (RemoveModel, ctx, _) => // we don't care if it doesn't exist
        ctx.thenPersist(ModelRemoved)(evt => ctx.reply(Done))
    }.onEvent {
      // Event handler for the ClientChanged event
      case (ModelCreated(id, description), Some(state)) =>
        Some(state.copy(models = models + ModelDescription(id, description)))
      case ModelRemoved(id: ModelId) =>
        Some(state.copy(models = models.filterNot(_.id == id)))
    }
  }
}








