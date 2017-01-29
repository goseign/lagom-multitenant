package optrak.lagomtest.datamodel.impl

import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, ReadSideProcessor}

import scala.concurrent.ExecutionContext

/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  * Manages list of valid products. Uses Cassandra queries
  */
private[impl] class ProductRepository(session: CassandraSession)(implicit ec: ExecutionContext) {



}

private[impl] class ItemEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[ProductEvent] {
  override def buildHandler(): ReadSideProcessor.ReadSideHandler[ProductEvent] = ???

  override def aggregateTags: Set[AggregateEventTag[ProductEvent]] = ???
}

