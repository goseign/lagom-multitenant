package optrak.lagomtest.products.impl

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
import akka.Done
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import optrak.lagomtest.products.api.{ProductCreationData, ProductCreationData$, ProductService}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import ProductTestCommon._
import optrak.lagomtest.datamodel.Models.Product

import scala.concurrent.Future

class ProductServiceRepositoryTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new ProductApplication(ctx) with LocalServiceLocator
  }

  val serviceClient = server.serviceClient.implement[ProductService]

  override protected def afterAll() = server.stop()

  def doCreate(product: Product): Future[Done] = {
    val fDone: Future[Done] = serviceClient.createProduct(tenantId, product.id).invoke(ProductCreationData(product.size, product.group))
    fDone
  }
/*

  "product service" should {

    "create product" in {

      doCreate("tim")
      doCreate("tom")
      Thread.sleep(20000)
      for {
        recoveredProducts <- serviceClient.getAllProducts.invoke()
      } yield { recoveredProducts.toSet should === (Set("tim", "tom")) }
    }

    "create many" in {
      val iSet = 1.to(200).map {i => i.toString }
      iSet.foreach { i => doCreate(i) }
      Thread.sleep(20000)
      for {
        recoveredProducts <- product.getAllProducts.invoke()
      } yield { recoveredProducts.toSet should === (Set("tim", "tom") ++ iSet) }

    }
  }
  */
}
