package optrak.lagomtest.tenant.impl

import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.{TransportErrorCode, TransportException}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import optrak.lagomtest.data.Data._
import TenantEvents.{TenantCreated, TenantEvent, ModelCreated, ModelRemoved}
import play.api.libs.json.{Format, Json}
import optrak.lagomtest.utils.JsonFormats
import optrak.lagomtest.tenant.api.{ModelCreated => ApiModelCreated}
import optrak.lagomtest.tenant.api
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */

case class TenantAlreadyExistsException(tenantId: TenantId) extends TransportException(TransportErrorCode.UnsupportedData, s"tentant $tenantId already exists")

class TenantEntity extends PersistentEntity {

  override type Command = TenantCommand
  override type Event = TenantEvent
  override type State = Option[Tenant]


  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState = None

  /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case None => noTenantYet
    case Some(s) => hasTenant
  }

  def noTenantYet: Actions = {
    Actions()
      .onCommand[CreateTenant, Done] {
      case (CreateTenant(id, description), ctx, _) =>
        ctx.thenPersist(TenantCreated(id, description))(evt =>
          ctx.reply(Done)
        )
    }
      .onEvent {
        case (TenantCreated(id, description), _) =>
          Some(Tenant(id, Set.empty, description))
      }
  }

  def hasTenant: Actions = {
    Actions()
      .onCommand[CreateModel, api.ModelCreated] {
      // Command handler for the ChangeTenant command
      case (CreateModel(id, description), ctx, _) =>
        ctx.thenPersist(ModelCreated(id, description))(_ =>
          ctx.reply(ApiModelCreated(id)))
    }.onCommand[RemoveModel, Done] {
      case (RemoveModel(id), ctx, _) => // we don't care if it doesn't exist
        ctx.thenPersist(ModelRemoved(id))(evt => ctx.reply(Done))
    }  .onCommand[CreateTenant, Done] {
      case (CreateTenant(id, description), ctx, _) =>
        throw new TenantAlreadyExistsException(id)


    }.onEvent {
      // Event handler for the TenantChanged event
      case (ModelCreated(id, description), Some(tenant)) =>
        Some(tenant.copy(models = tenant.models + PlanDescription(id, description)))
      case (ModelRemoved(id: PlanId), Some(tenant)) =>
        Some(tenant.copy(models = tenant.models.filterNot(_.id == id)))
    }
  }
}

// --------------------------------- internal commands
sealed trait TenantCommand
case class CreateTenant(id: String, description: String) extends TenantCommand with ReplyType[Done]
case class CreateModel(id: PlanId, description: String) extends TenantCommand with ReplyType[api.ModelCreated]
case class RemoveModel(id: PlanId) extends TenantCommand with ReplyType[Done]

object CreateTenant {
  implicit def format: Format[CreateTenant] = Json.format[CreateTenant]
}

object CreateModel {
  implicit def format: Format[CreateModel] = Json.format[CreateModel]
}

object RemoveModel {
  implicit def format: Format[RemoveModel] = Json.format[RemoveModel]
}





