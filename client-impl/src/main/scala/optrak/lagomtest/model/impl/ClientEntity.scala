package optrak.lagomtest.model.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import optrak.lagomtest.model.Models.{Client, ClientId, ModelId}
import optrak.lagomtest.model.impl.ClientEvents.{ClientChanged, ClientEvent}
import play.api.libs.json.{Format, Json}

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ClientEntity extends PersistentEntity {


  override type Command = ClientCommand
  override type Event = ClientEvent
  override type State = ClientState

  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState = EmptyClient

  /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case EmptyClient => noClientYet
    case WithClient(client) => hasClient
  }

  def noClientYet: Actions = {
    Actions()
    .onCommand[SetClient, Done] {
      case (SetClient(clientUpdate), ctx, _) =>
        ctx.thenPersist(ClientChanged(Client(entityId, clientUpdate)))(evt =>
          ctx.reply(Done)
        )
    }.onReadOnlyCommand[GetClient.type, ClientState] {
      case (GetClient, ctx, state) =>
        ctx.reply(EmptyClient)

    }
    .onEvent{
      case (ClientChanged(client), _) =>
        WithClient(client)
    }
  }

  def hasClient: Actions =
  {
    Actions().onCommand[SetClient, Done] {

      // Command handler for the ChangeClient command
      case (SetClient(clientUpdate), ctx, state) =>
        val newClient = Client(entityId, clientUpdate)
        if (WithClient(newClient) != state)
          ctx.thenPersist(ClientChanged(newClient))(_ => ctx.reply(Done))
        else ctx.done

//    }.onReadOnlyCommand[GetClient.type, ClientDesc] {
    }.onReadOnlyCommand[GetClient.type, ClientState] {
      case (GetClient, ctx, state) =>
        ctx.reply(state)

    }.onEvent {

      // Event handler for the ClientChanged event
      case (ClientChanged(newClient), state) =>
        // We simply update the current state to use the greeting message from
        // the event.
        WithClient(newClient)

    }
  }
}

// --------------- commands -------------------------
sealed trait ClientCommand

case class CreateClient(id: ClientId, description: String)

case class CreateModel(id: ClientId, modelDescription: String)

case class RemoveModel(id: ClientId, modelId: ModelId)

object CreateClient {
  implicit val format: Format[CreateClient] = Json.format[CreateClient]
}

object CreateModel {
  implicit val format: Format[CreateModel] = Json.format[CreateModel]
}

object RemoveModel {
  implicit val format: Format[RemoveModel] = Json.format[RemoveModel]
}

// --------------- state -------------------------
sealed trait ClientState

case class WithClient(client: Client) extends ClientState

object WithClient {
  implicit val format: Format[WithClient] = Json.format[WithClient]
}

case object EmptyClient extends ClientState {
  implicit val format: Format[EmptyClient.type] = singletonFormat(EmptyClient)
}






