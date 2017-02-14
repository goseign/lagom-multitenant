package optrak.lagomtest.tenant.api

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
trait TenantService extends Service {

  def createTenant(clientId: TenantId): ServiceCall[TenantCreationData, Done]

  def createModel(clientId: TenantId): ServiceCall[ModelCreationData, ModelCreated]

  def removeModel(clientId: TenantId, modelId: ModelId): ServiceCall[NotUsed, Done]

  def getTenant(clientId: TenantId): ServiceCall[NotUsed, Tenant]

  def getAllTenants: ServiceCall[NotUsed, Seq[TenantId]]

  override final def descriptor = {
    import Service._

    named("product").withCalls(
      pathCall("/optrak.model.api/createTenant/:id", createTenant _),
      pathCall("/optrak.model.api/createModel/:id", createModel _),
      pathCall("/optrak.model.api/removeModel/:clientId/:modelId", removeModel _),
      pathCall("/optrak.model.api/client/:id", getTenant _),
      pathCall("/optrak.model.api/client", getAllTenants _)

    ).withAutoAcl(true)
  }

}

sealed trait TenantApiCommand
// nb these could easily be passed in query params but in real world will have much greater data payload
case class TenantCreationData(description: String) extends TenantApiCommand with ReplyType[Done]
case class ModelCreationData(description: String) extends TenantApiCommand with ReplyType[ModelCreated]

object TenantCreationData {
  implicit def format: Format[TenantCreationData] = Json.format[TenantCreationData]
}

object ModelCreationData {
  implicit def format: Format[ModelCreationData] = Json.format[ModelCreationData]
}

// responses
case class ModelCreated(id: ModelId)

object ModelCreated {
  implicit def format: Format[ModelCreated] = Json.format[ModelCreated]
}
