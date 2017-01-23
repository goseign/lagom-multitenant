package optrak.lagom.projects.impl

import com.lightbend.lagom.scaladsl.playjson.{SerializerRegistry, Serializers}
import optrak.lagom.products.api.Project
import optrak.lagom.products.api.Project._

import scala.collection.immutable.Seq
import JsonFormats._
import optrak.lagom.projects.impl.ProjectEvents.ProjectChanged
import optrak.lagom.projects.impl.ProjectEvents.ProjectChanged
import play.api.libs.json.{Format, Json}

/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
class ProjectSerializerRegistry extends SerializerRegistry {

  implicitly[Format[Project]]

  override def serializers: Seq[Serializers[_]] = {
    val res = Seq(
      Serializers[SetProject],
      Serializers[Project],
      Serializers[WithProject],
      Serializers[ProjectChanged],
      Serializers[EmptyProject.type],
      Serializers[GetProject.type]
    )
    res
  }
}