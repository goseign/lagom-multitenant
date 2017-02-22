package optrak.lagomtest.products.impl

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import optrak.lagomtest.data.Data.{Product, ProductId}
import optrak.lagomtest.products.impl.ProductEvents._
import optrak.lagomtest.data.DataJson._
import optrak.lagomtest.products.api.{ProductIds, ProductStatus, ProductStatuses}
import optrak.lagomtest.products.impl.TenantProductDirectoryEntity.Innards

import scala.collection.immutable.Seq

/**
  * Created by tim on 22/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
object ProductSerializerRegistry extends JsonSerializerRegistry {

  override def serializers: Seq[JsonSerializer[_]] = {
    val res = Seq(
      JsonSerializer[CreateProduct],
      JsonSerializer[UpdateProductSize],
      JsonSerializer[UpdateProductGroup],
      JsonSerializer[CancelProduct],
      JsonSerializer[GetProduct.type],
      JsonSerializer[Product],
      JsonSerializer[ProductCreated],
      JsonSerializer[ProductSizeUpdated],
      JsonSerializer[ProductGroupUpdated],
      JsonSerializer[ProductCancelled],
      JsonSerializer[ProductAddedToDirectory],
      JsonSerializer[ProductCancelledInDirectory],
      JsonSerializer[WrappedCreateProduct],
      JsonSerializer[WrappedCancelProduct],
      JsonSerializer[GetAllProducts.type],
      JsonSerializer[GetLiveProducts.type],
      JsonSerializer[ProductStatus],
      JsonSerializer[ProductStatuses],
      JsonSerializer[ProductIds],
      JsonSerializer[Innards]

    )
    res
  }
}