package optrak.lagomtest.model.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import optrak.lagomtest.model.Models._
import play.api.libs.json.{Format, Json}
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
trait ModelService extends Service {

  def createClient(clientId: ClientId): ServiceCall[CreateClient, Done]

  def createModel(clientId: ClientId): ServiceCall[CreateModel, CreatedModel]

  def removeModel(modelId: ModelId): ServiceCall[NotUsed, Done]



  override final def descriptor = {
    import Service._

    named("product").withCalls(
      pathCall("/optrak.model.api/createClient/:id", createClient _),
      pathCall("/optrak.model.api/createModel/:id", createModel _),
      pathCall("/optrak.model.api/removeModel/:id", removeModel _)

    ).withAutoAcl(true)
  }

}

sealed trait ClientCommand
case class CreateClient(id: ClientId, description: String) extends ClientCommand
case class CreateModel(description: String) extends ClientCommand
case object RemoveModel extends ClientCommand

object CreateClient {
  implicit def format: Format[CreateClient] = Json.format[CreateClient]
}

object CreateModel {
  implicit def format: Format[CreateModel] = Json.format[CreateModel]
}

// responses
case class CreatedModel(id: String)

object CreatedModel {
  implicit def format: Format[CreatedModel] = Json.format[CreatedModel]
}
