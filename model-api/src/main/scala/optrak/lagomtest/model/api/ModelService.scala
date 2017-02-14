package optrak.lagomtest.model.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import optrak.lagomtest.datamodel.Models._
import optrak.lagomtest.datamodel.ModelsJson._
import optrak.lagomtest.model.api.ModelEvents._
import play.api.libs.json.{Format, Json}

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */

object ModelService {
  def topicName = "ModelTopic"
}

trait ModelService extends Service {

  def createModel(modelId: ModelId): ServiceCall[String, Done]

  // product CRUD
  def addProduct(modelId: ModelId): ServiceCall[Product, Done]

  def updateProduct(modelId: ModelId): ServiceCall[Product, Done]

  def addOrUpdateProduct(modelId: ModelId): ServiceCall[Product, Done]

  def removeProduct(modelId: ModelId): ServiceCall[ProductId, Done]

  // Site CRUD

  def addSite(modelId: ModelId): ServiceCall[Site, Done]

  def updateSite(modelId: ModelId): ServiceCall[Site, Done]

  def addOrUpdateSite(modelId: ModelId): ServiceCall[Site, Done]

  def removeSite(modelId: ModelId): ServiceCall[SiteId, Done]

  // query methods, getting one or all the products from the model
  
  def product(modelId: ModelId, productId: ProductId): ServiceCall[NotUsed, Product]

  def products(modelId: ModelId): ServiceCall[NotUsed, Seq[Product]]

  def site(modelId: ModelId, siteId: SiteId): ServiceCall[NotUsed, Site]

  def sites(modelId: ModelId): ServiceCall[NotUsed, Seq[Site]]


  override final def descriptor = {
    import Service._

    named("model").withCalls(
      pathCall("/optrak.model.api/createModel/:id", createModel _),

      pathCall("/optrak.model.api/addProduct/:id", addProduct _),
      pathCall("/optrak.model.api/updateProduct/:id", updateProduct _),
      pathCall("/optrak.model.api/addOrUpdateProduct/:id", addOrUpdateProduct _),
      pathCall("/optrak.model.api/removeProduct/:id", removeProduct _),

      pathCall("/optrak.model.api/addSite/:id", addSite _),
      pathCall("/optrak.model.api/updateSite/:id", updateSite _),
      pathCall("/optrak.model.api/addOrUpdateSite/:id", addOrUpdateSite _),
      pathCall("/optrak.model.api/removeSite/:id", removeSite _),

      pathCall("/optrak.model.api/product/:modelId/:productId", product _),
      pathCall("/optrak.model.api/product/:modelId", products _),
      pathCall("/optrak.model.api/site/:modelId/:siteId", site _),
      pathCall("/optrak.model.api/site/:modelId", sites _)


    ).withTopics(
      topic(ModelService.topicName, modelEvents)
      .addProperty(KafkaProperties.partitionKeyStrategy,
      PartitionKeyStrategy[ModelEvent](_.modelId.toString))
    )
    .withAutoAcl(true)

  }
  def modelEvents() : Topic[ModelEvent]

}

