package optrak.lagomtest.client.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import optrak.lagomtest.datamodel.Models._
import optrak.lagomtest.utils.JsonFormats
import play.api.libs.json.{Format, Json}
import optrak.lagomtest.datamodel.ModelsJson._
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
trait ClientService extends Service {

  def createClient(clientId: ClientId): ServiceCall[CreateClient, Done]

  def createModel(clientId: ClientId): ServiceCall[CreateModel, ModelCreated]

  def removeModel(clientId: ClientId): ServiceCall[RemoveModel, Done]

  def getClient(clientId: ClientId): ServiceCall[NotUsed, Client]

  def getAllClients: ServiceCall[NotUsed, Seq[ClientId]]

  override final def descriptor = {
    import Service._

    named("product").withCalls(
      pathCall("/optrak.model.api/createClient/:id", createClient _),
      pathCall("/optrak.model.api/createModel/:id", createModel _),
      pathCall("/optrak.model.api/removeModel/:id", removeModel _),
      pathCall("/optrak.model.api/client/:id", getClient _),
      pathCall("/optrak.model.api/client", getAllClients _)

    ).withAutoAcl(true)
  }

}

sealed trait ClientApiCommand
case class CreateClient(description: String) extends ClientApiCommand with ReplyType[Done]
case class CreateModel(description: String) extends ClientApiCommand with ReplyType[ModelCreated]
case class RemoveModel(id: ModelId) extends ClientApiCommand with ReplyType[Done]

object CreateClient {
  implicit def format: Format[CreateClient] = Json.format[CreateClient]
}

object CreateModel {
  implicit def format: Format[CreateModel] = Json.format[CreateModel]
}

object RemoveModel {
  implicit def format: Format[RemoveModel] = Json.format[RemoveModel]

}

// responses
case class ModelCreated(id: ModelId)

object ModelCreated {
  implicit def format: Format[ModelCreated] = Json.format[ModelCreated]
}
