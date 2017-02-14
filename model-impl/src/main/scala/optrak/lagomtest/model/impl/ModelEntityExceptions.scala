package optrak.lagomtest.model.impl

import com.lightbend.lagom.scaladsl.api.transport.{TransportErrorCode, TransportException}
import optrak.lagomtest.datamodel.Models.{ModelDescription, ModelId, ProductId, SiteId}

/**
  * Created by tim on 30/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object ModelEntityExceptions {
  case class ModelAlreadyExistsException(modelDescription: ModelDescription) extends TransportException(TransportErrorCode.UnsupportedData, s"model $modelDescription already exists")
  case class ProductAlreadyDefinedException(productId: ProductId, modelId: ModelId) extends TransportException(TransportErrorCode.UnsupportedData, s"product $productId already defined in model $modelId")
  case class ProductNotDefinedException(productId: ProductId, modelId: ModelId) extends TransportException(TransportErrorCode.UnsupportedData, s"product $productId nod defined in model $modelId")
  case class SiteAlreadyDefinedException(siteId: SiteId, modelId: ModelId) extends TransportException(TransportErrorCode.UnsupportedData, s"site $siteId already defined in model $modelId")
  case class SiteNotDefinedException(siteId: SiteId, modelId: ModelId) extends TransportException(TransportErrorCode.UnsupportedData, s"site $siteId nod defined in model $modelId")

}
