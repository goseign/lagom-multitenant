package optrak.lagomtest.sites.impl

import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.{TransportErrorCode, TransportException}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import grizzled.slf4j.Logging
import optrak.lagomtest.data.Data._
import optrak.lagomtest.sites.impl.SiteEvents._
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */


case class SiteAlreadyExistsException(tenantId: TenantId, siteId: SiteId)
  extends TransportException(TransportErrorCode.UnsupportedData, s"site $siteId for tenant $tenantId already exists")

class SiteEntity extends PersistentEntity with Logging {

  override type Command = SiteCommand
  override type Event = SiteEvent
  override type State = Option[Site]


  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState = None
  
  private val getSiteCommand = Actions().onReadOnlyCommand[GetSite.type, Option[Site]] {
    case (GetSite, ctx, state) => ctx.reply(state)
  }
      /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case None => noSiteYet
    case Some(s) => hasSite
  }

  def noSiteYet: Actions = {
    Actions()
      .onCommand[CreateSite, Done] {
      case (CreateSite(tenantId, id, postcode), ctx, _) =>
        ctx.thenPersist(SiteCreated(tenantId, id, postcode)) { evt =>
          logger.debug(s"creating site $tenantId $id")
          ctx.reply(Done)
        }
    }.onEvent {
        case (SiteCreated(tenantId, id, postcode), _) =>
          val update = Some(Site(id, postcode))
          logger.debug(s"updated model for $id")
          update
      }.orElse(getSiteCommand)

  }

  def hasSite: Actions = {
    Actions()
      .onCommand[CreateSite, Done] {
      case (CreateSite(tenantId, id, postcode), ctx, _) =>
        throw new SiteAlreadyExistsException(tenantId, id)
    }.onCommand[UpdateSitePostcode, Done] {
      case (UpdateSitePostcode(tenantId, id, newPostcode), ctx, _) =>
        ctx.thenPersist(SitePostcodeUpdated(tenantId, id, newPostcode))(_ =>
          ctx.reply(Done))
    }.onEvent {
      // Event handler for the SiteChanged event
      case (SitePostcodeUpdated(tenantId, id, newPostcode), Some(site)) =>
        Some(site.copy(postcode = newPostcode))
    }.orElse(getSiteCommand)
  }
}

// --------------------------------- internal commands

sealed trait SiteCommand

sealed trait SiteDoCommand extends SiteCommand with ReplyType[Done]
case class CreateSite(tenantId: TenantId, id: String, postcode: String) extends SiteDoCommand
case class UpdateSitePostcode(tenantId: TenantId, id: String, newPostcode: String) extends SiteDoCommand

case object GetSite extends SiteCommand with ReplyType[Option[Site]]



