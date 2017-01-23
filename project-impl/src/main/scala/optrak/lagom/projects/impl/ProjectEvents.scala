package optrak.lagom.projects.impl

import com.lightbend.lagom.scaladsl.playjson.Jsonable
import optrak.lagom.products.api.Project
import play.api.libs.json.{Format, Json}
  /**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object ProjectEvents {
  sealed trait ProjectEvent extends Jsonable

  case class ProjectChanged(product: Project) extends ProjectEvent

  object ProjectChanged {
    implicit val format: Format[ProjectChanged] = Json.format[ProjectChanged]
  }

}
