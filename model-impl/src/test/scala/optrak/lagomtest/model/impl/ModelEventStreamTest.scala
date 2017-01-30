package optrak.lagomtest.model.impl

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
import akka.Done
import akka.stream.scaladsl.Sink
import com.datastax.driver.core.utils.UUIDs
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.{ServiceTest, TestTopicComponents}
import optrak.lagomtest.datamodel.Models.{ModelDescription, Product}
import optrak.lagomtest.model.api.ModelEvents.ModelCreated
import optrak.lagomtest.model.api.ModelService
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class ModelEventStreamTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new ModelApplication(ctx) with LocalServiceLocator with TestTopicComponents
  }

  val modelService = server.serviceClient.implement[ModelService]
  val product1 = Product("product1", 1, "g1")

  override protected def afterAll() = server.stop()

  val modelId = UUIDs.timeBased()

  implicit val mat = server.materializer

  "model service" should {

    "create model" in {
      for {
        answer <- modelService.createModel(modelId).invoke("my model")
        event <- modelService.modelTopic.subscribe.atMostOnceSource
          .runWith(Sink.head)
      } yield {
        answer should === (Done)
        event should === (ModelCreated(ModelDescription(modelId, "my model")))
      }
    }

    "add product" in {
      for {
        answer <- modelService.addProduct(modelId).invoke(product1)
      } yield {
        answer should === (Done)
      }
    }

    "removed model" in {
      for {
        answer <- modelService.removeProduct(modelId).invoke(product1.id)
      } yield {
        answer should === (Done)
      }
    }
  }
}