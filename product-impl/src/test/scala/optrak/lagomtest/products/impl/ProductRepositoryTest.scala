package optrak.lagomtest.products.impl

import java.util.concurrent.atomic.AtomicInteger

import akka.persistence.query.Sequence
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server.LagomApplication
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import optrak.lagomtest.datamodel.Models.ProductId
import optrak.lagomtest.products.impl.ProductEvents.{ProductCancelled, ProductCreated, ProductEvent}
import optrak.lagomtest.utils.ReadSideTestDriver
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.libs.ws.ahc.AhcWSComponents
import ProductTestCommon._
import optrak.lagomtest.products.api.ProductStatus

import scala.concurrent.Future

class ProductRepositoryTest extends AsyncWordSpec with BeforeAndAfterAll with Matchers {

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
    new LagomApplication(ctx) with ProductComponents with AhcWSComponents {
      override def serviceLocator = NoServiceLocator
      override lazy val readSide: ReadSideTestDriver = new ReadSideTestDriver
    }
  }

  override def afterAll() = server.stop()

  private val testDriver = server.application.readSide
  private val productRepository = server.application.productRepository
  private val offset = new AtomicInteger()


  "The product event processor" should {
    "create a product" in {
      val productCreated = ProductCreated(tenantId, product1Id, product1Size, group1)
      for {
        _ <- feed(entityId(tenantId, product1Id), productCreated)
        products <- getProducts
      } yield {
        products should contain only (ProductStatus(productCreated.id, false))
      }
    }

    "create another product" in {
      val productCreated = ProductCreated(tenantId, product2Id, product2Size, group2)
      val productCancelled = ProductCancelled(tenantId, product1Id)
      for {
        _ <- feed(entityId(tenantId, product2Id), productCreated)
        _ <- feed(entityId(tenantId, product1Id), productCancelled)
        allProducts <- getProducts
        liveProducts <- getLiveProducts
      } yield {
        allProducts should contain (ProductStatus(product1Id, true))
        allProducts should contain (ProductStatus(product2Id, false))
        liveProducts should contain only (product2Id)
      }
    }
  }

  private def getProducts = {
    productRepository.selectProductsForTenant(tenantId)
  }

  private def getLiveProducts = {
    productRepository.selectLiveProductsForTenant(tenantId)
  }

  private def feed(productId: ProductId, event: ProductEvent) = {
    testDriver.feed(productId.toString, event, Sequence(offset.getAndIncrement))
  }


}