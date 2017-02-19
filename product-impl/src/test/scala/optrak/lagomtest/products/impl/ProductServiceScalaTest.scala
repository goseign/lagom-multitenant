package optrak.lagomtest.products.impl

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
import akka.Done
import com.lightbend.lagom.scaladsl.api.transport.TransportException
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import optrak.lagomtest.products.api.{ProductCreationData, ProductService, ProductStatus, ProductStatuses}
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

/*
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
        te.toString should include("product product1 for tenant tenant1 already exists")
      }
    }

  }
  */
  "reading" should {
    def createP(implicit arb: Arbitrary[ProductCreationData]): Option[ProductCreationData] =
      arb.arbitrary.sample

    "create multiple products for single tenant" in {

      implicitly[Arbitrary[ProductCreationData]]

      // we're going to generate some arbitrary products then check that they actually got created
      val cps : List[ProductCreationData] = 0.to(10).flatMap( i => createP ).toList
      val products: List[(ProductId, ProductCreationData)] = cps.zipWithIndex.map(t => (t._2.toString, t._1))
      val productsCreated = products.map {t => client.createProduct(tenantId, t._1).invoke(t._2)}

      for {
        seq <- Future.sequence(productsCreated)
        allProducts <- {
          Thread.sleep(4000)
          client.getProductsForTenant(tenantId).invoke()
        }
      } yield {
        val ap: ProductStatuses = allProducts
        println(s"all products is $allProducts")

        products.foreach { p =>
          val found = ap.statuses.find(_.productId == p._1)
          found should === (Some(ProductStatus(p._1, false)))
        }
        true should === (true)
      }

    }

    "cancel selected products" in {

      val cps : List[ProductCreationData] = 20.to(30).flatMap( i => createP ).toList
      val products: List[(ProductId, ProductCreationData)] = cps.zipWithIndex.map(t => ((t._2 + 20).toString, t._1))
      val productsCreated = products.map {t =>
        Thread.sleep(100)
        client.createProduct(tenantId, t._1).invoke(t._2)
      }

      for {
        seq <- Future.sequence(productsCreated)

        allProducts <- client.getProductsForTenant(tenantId).invoke()
        hid = allProducts.statuses.head.productId
      _ = println(s"head id is $hid")
        cancelled <- client.cancelProduct(tenantId, hid).invoke()
        checkCancelled <- client.getProduct(tenantId, hid).invoke()
        _ = println(s"got ceancelled returns $checkCancelled")
        liveProducts <- {
          Thread.sleep(3000)
          client.getLiveProductsForTenant(tenantId).invoke()
        }
      } yield {
        checkCancelled.cancelled === true
        println(s"live products is $liveProducts")
        liveProducts.ids.size should === (allProducts.statuses.size - 1)
      }

    }
  }

}
