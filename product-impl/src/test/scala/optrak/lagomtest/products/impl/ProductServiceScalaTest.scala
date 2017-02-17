package optrak.lagomtest.products.impl

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.TransportException
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import optrak.lagomtest.products.api.{ProductCreationData, ProductService, ProductStatus}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import ProductTestCommon._
import optrak.lagomtest.datamodel.Models.{Product, ProductId}
import org.scalacheck._
import org.scalacheck.Shapeless._
// import org.scalacheck.Gen._

import scala.concurrent.Future

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

    "create and retrieve product" in {
      for {
        answer <- client.createProduct(tenantId, product1Id).invoke(createProductData(product1))
        retrieved <- client.getProduct(tenantId, product1Id).invoke()
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

  "reading" should {
    "create and retrieve multiple products for single tenant" in {

      implicitly[Arbitrary[ProductCreationData]]

      def createP(implicit arb: Arbitrary[ProductCreationData]): Option[ProductCreationData] =
        arb.arbitrary.sample

      // we're going to generate some arbitrary products then check that they actually got created
      val cps : List[ProductCreationData] = 0.to(10).flatMap( i => createP ).toList
      val products: List[(ProductId, ProductCreationData)] = cps.zipWithIndex.map(t => (t._2.toString, t._1))
      val productsCreated = products.map {t => client.createProduct(tenantId, t._1).invoke(t._2)}

      for {
        seq <- Future.sequence(productsCreated)
        allProducts <- client.getProductsForTenant(tenantId).invoke()
      } yield {
        val ap: List[ProductStatus] = allProducts

        products.foreach { p =>
          val found = ap.find(_.productId == p._1)
          found should === (true)
        }
        true should === (true)
      }

    }

    "cancel selected tenants" in {

      implicitly[Arbitrary[ProductCreationData]]

      def createP(implicit arb: Arbitrary[ProductCreationData]): Option[ProductCreationData] =
        arb.arbitrary.sample

      // we're going to generate some arbitrary products then check that they actually got created
      val cps : List[ProductCreationData] = 0.to(10).flatMap( i => createP ).toList
      val products: List[(ProductId, ProductCreationData)] = cps.zipWithIndex.map(t => (t._2.toString, t._1))
      val productsCreated = products.map {t => client.createProduct(tenantId, t._1).invoke(t._2)}

      for {
        seq <- Future.sequence(productsCreated)
        allProducts <- client.getProductsForTenant(tenantId).invoke()
      } yield {
        val ap: List[ProductStatus] = allProducts

        products.foreach { p =>
          val found = ap.find(_.productId == p._1)
          found should === (true)
        }
        true should === (true)
      }

    }

  }
}
