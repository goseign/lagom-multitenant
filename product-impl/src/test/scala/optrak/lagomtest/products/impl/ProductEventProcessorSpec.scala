package optrak.lagomtest.products.impl

import java.util.concurrent.atomic.AtomicInteger

import akka.persistence.query.Sequence
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server.LagomApplication
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import optrak.lagomtest.datamodel.Models.ProductId
import optrak.lagomtest.products.impl.ProductEvents.ProductCreated
import optrak.lagomtest.utils.ReadSideTestDriver
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.libs.ws.ahc.AhcWSComponents

class ProductEventProcessorSpec extends AsyncWordSpec with BeforeAndAfterAll with Matchers {

  private val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
    new LagomApplication(ctx) with ProductComponents with AhcWSComponents {
      override def serviceLocator = NoServiceLocator
      override lazy val readSide: ReadSideTestDriver = new ReadSideTestDriver
    }
  }
/*
  override def afterAll() = server.stop()

  private val testDriver = server.application.readSide
  private val productRepository = server.application.productRepository
  private val offset = new AtomicInteger()


  "The product event processor" should {
    "create a product" in {
      val productCreated = ProductCreated("tim", "hello")
      for {
        _ <- feed(productCreated.id, productCreated)
        products <- getProducts
      } yield {
        products should contain only productCreated.id
      }
    }

  }

  private def getProducts = {
    productRepository.selectAllProducts
  }

  private def feed(productId: ProductId, event: ProductEvent) = {
    testDriver.feed(productId.toString, event, Sequence(offset.getAndIncrement))
  }
  *
  "this test" should {
    "fail until you fix it" in {
      true === false
    }
  }
  */
}