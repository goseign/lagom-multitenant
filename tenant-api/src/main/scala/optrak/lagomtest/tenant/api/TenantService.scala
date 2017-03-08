package optrak.lagomtest.tenant.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import optrak.lagomtest.data.Data._
import play.api.libs.json._
import optrak.scalautils.json.JsonImplicits._
import optrak.lagom.utils .PlayJson4s._
import optrak.scalautils.json.JsonParser
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
trait TenantService extends Service {

  def createTenant(tenantId: TenantId): ServiceCall[TenantCreationData, Done]

  def createModel(tenantId: TenantId): ServiceCall[ModelCreationData, ModelCreated]

  def removeModel(tenantId: TenantId, modelId: PlanId): ServiceCall[NotUsed, Done]

  def getTenant(tenantId: TenantId): ServiceCall[NotUsed, Tenant]

  def getAllTenants: ServiceCall[NotUsed, Seq[TenantId]]

  implicitly[JsonParser[ModelCreationData]]
  implicitly[JsonParser[ModelCreated]]

  override final def descriptor = {
    import Service._

    named("product").withCalls(
      pathCall("/optrak.model.api/createTenant/:id", createTenant _),
      pathCall("/optrak.model.api/createModel/:id", createModel _),
      pathCall("/optrak.model.api/removeModel/:tenantId/:modelId", removeModel _),
      pathCall("/optrak.model.api/tenant/:id", getTenant _),
      pathCall("/optrak.model.api/tenant", getAllTenants _)

    ).withAutoAcl(true)
  }

}

sealed trait TenantApiCommand
// nb these could easily be passed in query params but in real world will have much greater data payload
case class TenantCreationData(description: String) extends TenantApiCommand with ReplyType[Done]
case class ModelCreationData(description: String) extends TenantApiCommand with ReplyType[ModelCreated]

// responses
case class ModelCreated(id: PlanId)
