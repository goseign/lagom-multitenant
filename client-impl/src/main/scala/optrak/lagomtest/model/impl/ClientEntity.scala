package optrak.lagomtest.model.impl

import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.{TransportErrorCode, TransportException}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import optrak.lagomtest.model.Models._
import optrak.lagomtest.model.impl.ClientEvents.{ClientCreated, ClientEvent, ModelCreated, ModelRemoved}
import play.api.libs.json.{Format, Json}
import optrak.lagomtest.utils.JsonFormats
import optrak.lagomtest.model.api.{CreateClient => ApiCreateClient, CreateModel => ApiCreateModel, ModelCreated => ApiModelCreated}
import optrak.lagomtest.model.ModelsJson._
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */

case class ClientAlreadyExistsException(clientId: ClientId) extends TransportException(TransportErrorCode.UnsupportedData, s"client $clientId already exists")

class ClientEntity extends PersistentEntity {

  override type Command = ClientCommand
  override type Event = ClientEvent
  override type State = ClientState


  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState = EmptyClientState

  /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case EmptyClientState => noClientYet
    case NonEmptyClientState(s) => hasClient
  }

  def noClientYet: Actions = {
    Actions()
      .onCommand[CreateClient, Done] {
      case (CreateClient(id, description), ctx, _) =>
        ctx.thenPersist(ClientCreated(id, description))(evt =>
          ctx.reply(Done)
        )
    }
      .onEvent {
        case (ClientCreated(id, description), _) =>
          NonEmptyClientState(Client(id, Set.empty, description))
      }
  }

  def hasClient: Actions = {
    Actions()
      .onCommand[CreateModel, ApiModelCreated] {
      // Command handler for the ChangeClient command
      case (CreateModel(id, description), ctx, _) =>
        ctx.thenPersist(ModelCreated(id, description))(_ =>
          ctx.reply(ApiModelCreated(id)))
    }.onCommand[RemoveModel, Done] {
      case (RemoveModel(id), ctx, _) => // we don't care if it doesn't exist
        ctx.thenPersist(ModelRemoved(id))(evt => ctx.reply(Done))
    }  .onCommand[CreateClient, Done] {
      case (CreateClient(id, description), ctx, _) =>
        throw new ClientAlreadyExistsException(id)


    }.onEvent {
      // Event handler for the ClientChanged event
      case (ModelCreated(id, description), NonEmptyClientState(client)) =>
        NonEmptyClientState(client.copy(models = client.models + ModelDescription(id, description)))
      case (ModelRemoved(id: ModelId), NonEmptyClientState(client)) =>
        NonEmptyClientState(client.copy(models = client.models.filterNot(_.id == id)))
    }
  }
}

sealed trait ClientState
case object EmptyClientState extends ClientState {
  implicit def format: Format[EmptyClientState.type] = JsonFormats.singletonFormat(EmptyClientState)
}
case class NonEmptyClientState(client: Client) extends ClientState
object NonEmptyClientState {
  implicit def format: Format[NonEmptyClientState] = Json.format[NonEmptyClientState]
}

// --------------------------------- internal commands
sealed trait ClientCommand
case class CreateClient(id: String, description: String) extends ClientCommand with ReplyType[Done]
case class CreateModel(id: ModelId, description: String) extends ClientCommand with ReplyType[ApiModelCreated]
case class RemoveModel(id: ModelId) extends ClientCommand with ReplyType[Done]

object CreateClient {
  implicit def format: Format[CreateClient] = Json.format[CreateClient]
}

object CreateModel {
  implicit def format: Format[CreateModel] = Json.format[CreateModel]
}

object RemoveModel {
  implicit def format: Format[RemoveModel] = Json.format[RemoveModel]
}





