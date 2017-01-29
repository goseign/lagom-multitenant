package optrak.lagomtest.client.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.datastax.driver.core._
import optrak.lagomtest.client.impl.ClientEvents.{ClientCreated, ClientEvent}
import optrak.lagomtest.datamodel.Models.{Client, ClientId}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  * objective of this repository is to hold list of all created clients
  *
  * We follow the example in online-auction item repository, separating out the actual getting of the data
  * from read processor listening to the add client messages
  */
class ClientRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  def selectAllClients: Future[Seq[ClientId]] = {
    session.selectAll(
      """
        | select * from clients
      """.stripMargin).map( rows => rows.map(_.getString("clientId")))
  }


}

/**
  * This follows closely the model from ItemEventProcessor in online auction
  * @param session
  * @param readSide
  * @param executionContext
  */
private class ClientEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit executionContext: ExecutionContext)
extends ReadSideProcessor[ClientEvent] {

  private var insertClientIdStatement : PreparedStatement = null

  // todo voodoo  - figure out what exactly is happening
  def aggregateTags = ClientEvent.Tag.allTags


  private def createTables() = {
    for {
      _ <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS clients (
          clientId text PRIMARY KEY
        )
      """)
    } yield Done
  }

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[ClientEvent] = {
    readSide.builder[ClientEvent]("clientRepositoryOffset")
    .setGlobalPrepare(createTables)
    .setPrepare(_ => prepareStatements())
    .setEventHandler[ClientCreated](e => insertClientId(e.event.id))
    .build
  }


  private def prepareStatements() = {
    // nb original (item repository) worked like this. The assignment to all vars executed only if all
    // prepared statements creation succeeded.
    // For a single prepared statement it's not required but we keep the pattern
    for {
      insertClientId <- session.prepare(
        """
          | Insert into clients(clientId) values (?)
        """.stripMargin)
    } yield {
      insertClientIdStatement = insertClientId
      Done
    }
  }

  // again copy pattern from item repository
  private def insertClientId(clientId: ClientId) = {
    println(s"insertClientId $clientId")
    Future.successful(List(insertClientIdStatement.bind(clientId)))
  }


}


