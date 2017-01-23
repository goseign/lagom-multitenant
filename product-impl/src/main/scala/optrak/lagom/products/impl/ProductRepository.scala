package optrak.lagom.products.impl

import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import optrak.lagom.products.impl.ProductEvents.ProductEvent
import optrak.lagom.products.impl.ProductEvents.ProductEvent

import scala.concurrent.ExecutionContext

/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  * Manages list of valid products. Uses Cassandra queries
  */
private[impl] class ProductRepository(session: CassandraSession)(implicit ec: ExecutionContext) {



}
