package optrak.lagomtest.vehicles.impl

import akka.Done
import com.datastax.driver.core._
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import grizzled.slf4j.Logging
import optrak.lagomtest.data.Data._
import optrak.lagomtest.vehicles.api.VehicleIds
import optrak.lagomtest.vehicles.impl.VehicleEvents.{VehicleCreated, VehicleEvent}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  *
  * objective of this repository is to hold list of all vehicles by
  *
  * We follow the example in online-auction item repository, separating out the actual getting of the data
  * from read processor listening to the add vehicle messages
  */
class VehicleRepository(session: CassandraSession)(implicit ec: ExecutionContext) extends Logging {

  def selectVehiclesForTenant(tenantId: TenantId): Future[VehicleIds] = {
    val queryRes = session.selectAll(
      s"""
        | select vehicleId from vehicles where tenantId = '$tenantId'
      """.
        stripMargin)
      queryRes.map(rows =>
        VehicleIds(rows.map { r =>
          val vehicleId = r.getString("vehicleId")
          vehicleId
        }.toSet)
      )
    }


}
/**
  * This follows closely the model from ItemEventProcessor in online auction
  * @param session
  * @param readSide
  * @param executionContext
  */
private class VehicleEventDbProcessor(session: CassandraSession,
                                      readSide: CassandraReadSide)
                                     (implicit executionContext: ExecutionContext)
extends ReadSideProcessor[VehicleEvent] with Logging {

  private var insertVehicleStatement : PreparedStatement = null

  // todo voodoo  - figure out what exactly is happening
  def aggregateTags = VehicleEvent.Tag.allTags


  private def createTables() = {
    for {
      res <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS vehicles (
          tenantId text,
          vehicleId text,
          PRIMARY KEY (tenantId, vehicleId)
        )
      """)
    } yield {
      logger.debug(s"createTable result is $res")
      Done
    }
  }

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[VehicleEvent] = {
    logger.debug(s"in buildHandler")
    readSide.builder[VehicleEvent]("vehicleRepositoryOffset")
    .setGlobalPrepare(createTables)
    .setPrepare(_ => prepareStatements())
      .setEventHandler[VehicleCreated](e => insertVehicle(e.event.tenantId, e.event.vehicleId))
    .build
  }


  private def prepareStatements() = {
    // nb original (item repository) worked like this. The assignment to all vars executed only if all
    // prepared statements creation succeeded.
    // For a single prepared statement it's not required but we keep the pattern
    logger.debug(s"preparing statuements")
    for {
      insertVehicle <- session.prepare(
        """
          | INSERT INTO vehicles(tenantId, vehicleId) VALUES (?, ?)
        """.stripMargin)
    } yield {
      insertVehicleStatement = insertVehicle
      logger.debug(s"vehicleed insert statment $insertVehicle and $insertVehicleStatement")
      Done
    }
  }


  // note we're inserting vehicle but we're also sending it to the TenantVehicleDirectory - just because
  // we are testing both methods
  private def insertVehicle(tenantId: TenantId, vehicleId: VehicleId) = {
    Future.successful(List(insertVehicleStatement.bind(tenantId, vehicleId)))

  }


}
