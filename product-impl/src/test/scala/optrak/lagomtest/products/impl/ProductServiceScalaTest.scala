package optrak.lagomtest.products.impl

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.TransportException
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import optrak.lagomtest.products.api.{ProductCreationData, ProductService}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import ProductTestCommon._
import optrak.lagomtest.datamodel.Models.Product

class ProductServiceScalaTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new ProductApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[ProductService]

  override protected def afterAll() = server.stop()
  
  def createProductData(product: Product) = ProductCreationData(product.size, product.group) 


  "product service" should {

    "create product" in {
      for {
        answer <- client.createProduct(tenantId, product1Id).invoke(createProductData(product1))
      } yield {
        answer should ===(Done)
      }
    }

    "complain about 2nd attempt create product" in {
      val exp = recoverToExceptionIf[TransportException](
      for {
        answer <- client.createProduct(tenantId, product1Id).invoke(createProductData(product1))
        answer2 <- client.createProduct(tenantId, product1Id).invoke(createProductData(product1))
      } yield {
        answer2 should ===(Done)
      })
      exp.map { te =>
        // println(s"te is code ${te.errorCode} message ${te.exceptionMessage}")
        te.toString should include("tim already exists")
      }
    }

  }
}
