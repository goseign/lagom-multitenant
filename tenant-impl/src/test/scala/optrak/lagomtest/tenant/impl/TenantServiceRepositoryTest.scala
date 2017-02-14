package optrak.lagomtest.tenant.impl

/**
  * Created by tim on 28/01/17.
  * Copyright Tim Pigden, Hertford UK
  */
import akka.Done
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import optrak.lagomtest.tenant.api.{TenantCreationData, TenantService}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

import scala.concurrent.Future

class TenantServiceRepositoryTest extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new TenantApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[TenantService]

  override protected def afterAll() = server.stop()

  val clientId = "tim"

  def doCreate(clientId: String): Future[Done] = {
    val fDone: Future[Done] = client.createTenant(clientId).invoke(TenantCreationData("my client"))
    fDone
  }


  "client service" should {

    "create client" in {

      doCreate("tim")
      doCreate("tom")
      Thread.sleep(20000)
      for {
        recoveredTenants <- client.getAllTenants.invoke()
      } yield { recoveredTenants.toSet should === (Set("tim", "tom")) }
    }

    "create many" in {
      val iSet = 1.to(200).map {i => i.toString }
      iSet.foreach { i => doCreate(i) }
      Thread.sleep(20000)
      for {
        recoveredTenants <- client.getAllTenants.invoke()
      } yield { recoveredTenants.toSet should === (Set("tim", "tom") ++ iSet) }

    }
  }
}
