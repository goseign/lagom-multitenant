package optrak.lagomtest.datamodel.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import optrak.lagomtest.datamodel.Models._
import play.api.libs.json.{Format, Json}

/**
  * Created by tim on 21/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
trait ModelService extends Service {

  def createClient(clientId: ClientId): ServiceCall[CreateClient, Done]

  def createModel(clientId: ClientId): ServiceCall[CreateModel, ModelCreated]

  def removeModel(modelId: ModelId)

  def addProduct(modelId: ModelId): ServiceCall[Product, Done]

  def updateProduct(modelId: ModelId): ServiceCall[Product, Done]

  def addOrUpdateProduct(modelId: ModelId): ServiceCall[Product, Done]

  def removeProduct(modelId: ModelId): ServiceCall[ProductId, Done]

  def addSite(modelId: ModelId): ServiceCall[Site, Done]

  def updateSite(modelId: ModelId): ServiceCall[Site, Done]

  def addOrUpdateSite(modelId: ModelId): ServiceCall[Site, Done]

  def removeSite(modelId: ModelId): ServiceCall[SiteId, Done]
  
  def product(modelId: ModelId, productId: ProductId): ServiceCall[NotUsed, Product]

  def products(modelId: ModelId): ServiceCall[NotUsed, Seq[Product]]

  def site(modelId: ModelId, siteId: SiteId): ServiceCall[NotUsed, Site]

  def sites(modelId: ModelId): ServiceCall[NotUsed, Seq[Site]]


  override final def descriptor = {
    import Service._

    named("product").withCalls(
      pathCall("/optrak.model.api/createClient/:id", createClient _),
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


    ).withAutoAcl(true)
  }

}

case class CreateClient(description: String)
case class CreateModel(description: String)
case class ModelCreated(id: String)


