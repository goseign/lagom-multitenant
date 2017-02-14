package optrak.lagomtest.modelreader.impl

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.{ServiceTest, TestTopicComponents}
import optrak.lagomtest.datamodel.Models.{Model, Product}
import optrak.lagomtest.model.api.ModelService
import optrak.lagomtest.modelreader.api.ModelReaderService
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class ModelReaderServiceTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new ModelReaderApplication(ctx) with LocalServiceLocator
  }

  val modelService = server.serviceClient.implement[ModelService]
  val modelReaderService = server.serviceClient.implement[ModelReaderService]
  val product1 = Product("product1", 1, "g1", false)

  override protected def afterAll() = server.stop()

  val modelId = UUIDs.timeBased()

  implicit val mat = server.materializer

  "model service" should {
    "create model" in {
      for {
        created <- modelService.createModel(modelId).invoke("my model")
        addedProduct <- modelService.addProduct(modelId).invoke(product1)
        fromReader <- {
          Thread.sleep(2000)
          modelReaderService.getModel(modelId).invoke()
        }
      } yield {
        fromReader should ===(Model(modelId, "my model", Map(product1.id -> product1)))
      }
    }
  }
}
