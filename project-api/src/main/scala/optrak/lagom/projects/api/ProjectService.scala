package optrak.lagom.projects.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}
import com.lightbend.lagom.scaladsl.playjson.{Jsonable, SerializerRegistry, Serializers}

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
trait ProjectService extends Service {

  def setProject(id: String): ServiceCall[ProjectUpdate, Done]

  def getProject(id: String): ServiceCall[NotUsed, Project]


  override final def descriptor = {
    import Service._

    named("product").withCalls(
      pathCall("/optrak.lagom.products.api/product/:id", setProject _),
      pathCall("/optrak.lagom.products.api/product/:id", getProject _ )
    ).withAutoAcl(true)
  }

}

case class ProjectUpdate(size: Int, group: String)

object ProjectUpdate{
  implicit val format: Format[ProjectUpdate] = Json.format[ProjectUpdate]
}

case class Project(id: String, size: Int, group: String)

object Project {
  def apply(id: String, productUpdate: ProjectUpdate) = {
    import productUpdate._
    new Project(id, size, group)
  }

  implicit val format: Format[Project] = Json.format[Project]
}


