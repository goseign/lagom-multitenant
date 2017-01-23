package optrak.lagom.projects.impl

import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import optrak.lagom.projects.impl.ProjectEvents.ProjectEvent
import optrak.lagom.projects.impl.ProjectEvents.ProjectEvent

import scala.concurrent.ExecutionContext

/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  * Manages list of valid products. Uses Cassandra queries
  */
private[impl] class ProjectRepository(session: CassandraSession)(implicit ec: ExecutionContext) {



}
