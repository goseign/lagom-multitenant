package optrak.lagomtest.model.impl


import akka.persistence.cassandra.session.scaladsl.CassandraSession
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.datastax.driver.core._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  * objective of this repository is to hold an overview of created clients
  *
class ClientRepository(session: CassandraSession) {

}
*/