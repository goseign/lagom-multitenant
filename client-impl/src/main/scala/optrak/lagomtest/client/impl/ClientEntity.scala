package optrak.lagomtest.client.impl

import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.{TransportErrorCode, TransportException}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import optrak.lagomtest.datamodel.Models._
import optrak.lagomtest.client.impl.ClientEvents.{ClientCreated, ClientEvent, ModelCreated, ModelRemoved}
import play.api.libs.json.{Format, Json}
import optrak.lagomtest.utils.JsonFormats
import optrak.lagomtest.client.api.{ModelCreated => ApiModelCreated}
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */

case class ClientAlreadyExistsException(clientId: ClientId) extends TransportException(TransportErrorCode.UnsupportedData, s"client $clientId already exists")

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
    case Some(s) => hasClient
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
          Some(Client(id, Set.empty, description))
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
      case (ModelCreated(id, description), Some(client)) =>
        Some(client.copy(models = client.models + ModelDescription(id, description)))
      case (ModelRemoved(id: ModelId), Some(client)) =>
        Some(client.copy(models = client.models.filterNot(_.id == id)))
    }
  }
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





