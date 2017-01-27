package optrak.lagom.projects.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{Jsonable, SerializerRegistry, Serializers}
import optrak.lagom.products.api.{Project, ProjectUpdate}
import play.api.libs.json.{Format, Json}
import JsonFormats._
import optrak.lagom.projects.impl.ProjectEvents.{ProjectChanged, ProjectEvent}
import optrak.lagom.projects.impl.ProjectEvents.ProjectEvent

import scala.collection.immutable.Seq

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ProjectEntity extends PersistentEntity {


  override type Command = ProjectCommand
  override type Event = ProjectEvent
  override type State = ProjectState

  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState = EmptyProject

  /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case EmptyProject => noProjectYet
    case WithProject(product) => hasProject
  }

  def noProjectYet: Actions = {
    Actions()
    .onCommand[SetProject, Done] {
      case (SetProject(productUpdate), ctx, _) =>
        ctx.thenPersist(ProjectChanged(Project(entityId, productUpdate)), evt =>
          ctx.reply(Done)
        )
    }.onReadOnlyCommand[GetProject.type, ProjectState] {
      case (GetProject, ctx, state) =>
        ctx.reply(EmptyProject)

    }
    .onEvent{
      case (ProjectChanged(product), _) =>
        WithProject(product)
    }
  }

  def hasProject: Actions =
  {
    Actions().onCommand[SetProject, Done] {

      // Command handler for the ChangeProject command
      case (SetProject(productUpdate), ctx, state) =>
        val newProject = Project(entityId, productUpdate)
        if (WithProject(newProject) != state)
          ctx.thenPersist(ProjectChanged(newProject),
            // Then once the event is successfully persisted, we respond with done.
            _ => ctx.reply(Done)
          )
        else ctx.done

//    }.onReadOnlyCommand[GetProject.type, ProjectDesc] {
    }.onReadOnlyCommand[GetProject.type, ProjectState] {
      case (GetProject, ctx, state) =>
        ctx.reply(state)

    }.onEvent {

      // Event handler for the ProjectChanged event
      case (ProjectChanged(newProject), state) =>
        // We simply update the current state to use the greeting message from
        // the event.
        WithProject(newProject)

    }
  }
}

// --------------- commands -------------------------
sealed trait ProjectCommand extends Jsonable

case object GetProject extends ProjectCommand with ReplyType[ProjectState] {
  implicit val format: Format[GetProject.type] = singletonFormat(GetProject)
}

case class SetProject(productUpdate: ProjectUpdate) extends ProjectCommand with ReplyType[Done]

object SetProject {
  implicit val format: Format[SetProject] = Json.format[SetProject]
}

// --------------- state -------------------------
sealed trait ProjectState extends Jsonable

case class WithProject(product: Project) extends ProjectState

object WithProject {
  implicit val format: Format[WithProject] = Json.format[WithProject]
}

case object EmptyProject extends ProjectState {
  implicit val format: Format[EmptyProject.type] = singletonFormat(EmptyProject)
}






