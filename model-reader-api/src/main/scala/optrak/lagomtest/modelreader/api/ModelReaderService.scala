package optrak.lagomtest.modelreader.api
import java.util.UUID

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import optrak.lagomtest.datamodel.Models._
import play.api.libs.json.{Format, Json}
import optrak.lagomtest.datamodel.ModelsJson._
/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  * Microservice who's purpose is to build readable Models
  * Essentially these are a duplicate of the ModelEntity but designed for fast read access.
  * MessageBroker (kafka) is used to receive the update ModelEvent stream
  */
trait ModelReaderService extends Service {

  def getModel(id: UUID): ServiceCall[NotUsed, Model]

  final override def descriptor = {
    import Service._

    named("modelreader").withCalls(
      pathCall("/api/model:id", getModel _)
    )
  }

}