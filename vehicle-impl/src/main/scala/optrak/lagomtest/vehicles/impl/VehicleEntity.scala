package optrak.lagomtest.vehicles.impl

import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.{TransportErrorCode, TransportException}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import grizzled.slf4j.Logging
import optrak.lagomtest.data.Data._
import optrak.lagomtest.vehicles.impl.VehicleEvents._
import play.api.libs.json.{Format, Json}
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */


case class VehicleAlreadyExistsException(tenantId: TenantId, vehicleId: VehicleId)
  extends TransportException(TransportErrorCode.UnsupportedData, s"vehicle $vehicleId for tenant $tenantId already exists")

class VehicleEntity extends PersistentEntity with Logging {

  override type Command = VehicleCommand
  override type Event = VehicleEvent
  override type State = Option[Vehicle]


  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState = None
  
  private val getVehicleCommand = Actions().onReadOnlyCommand[GetVehicle.type, Option[Vehicle]] {
    case (GetVehicle, ctx, state) => ctx.reply(state)
  }
      /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case None => noVehicleYet
    case Some(s) => hasVehicle
  }

  def noVehicleYet: Actions = {
    Actions()
      .onCommand[CreateVehicle, Done] {
      case (CreateVehicle(tenantId, id, capacity), ctx, _) =>
        ctx.thenPersist(VehicleCreated(tenantId, id, capacity)) { evt =>
          logger.debug(s"creating vehicle $tenantId $id")
          ctx.reply(Done)
        }
    }.onEvent {
        case (VehicleCreated(tenantId, id, capacity), _) =>
          val update = Some(Vehicle(id, capacity))
          logger.debug(s"updated model for $id")
          update
      }.orElse(getVehicleCommand)

  }

  def hasVehicle: Actions = {
    Actions()
      .onCommand[CreateVehicle, Done] {
      case (CreateVehicle(tenantId, id, capacity), ctx, _) =>
        throw new VehicleAlreadyExistsException(tenantId, id)
    }.onCommand[UpdateVehicleCapacity, Done] {
      case (UpdateVehicleCapacity(tenantId, id, newCapacity), ctx, _) =>
        ctx.thenPersist(VehicleCapacityUpdated(tenantId, id, newCapacity))(_ =>
          ctx.reply(Done))
    }.onEvent {
      // Event handler for the VehicleChanged event
      case (VehicleCapacityUpdated(tenantId, id, newCapacity), Some(vehicle)) =>
        Some(vehicle.copy(capacity = newCapacity))
    }.orElse(getVehicleCommand)
  }
}

// --------------------------------- internal commands

sealed trait VehicleCommand

sealed trait VehicleDoCommand extends VehicleCommand with ReplyType[Done]
case class CreateVehicle(tenantId: TenantId, id: String, capacity: Int) extends VehicleDoCommand
case class UpdateVehicleCapacity(tenantId: TenantId, id: String, newCapacity: Int) extends VehicleDoCommand

case object GetVehicle extends VehicleCommand with ReplyType[Option[Vehicle]]





