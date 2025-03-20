import ai.bitflow.comfyui.multi.gateway.dao.CmfyHostChckDao
import ai.bitflow.comfyui.multi.gateway.dao.CmfyWbskClnt
import ai.bitflow.comfyui.multi.gateway.srvc.GnrtJobSrvc
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.websockets.next.WebSocketClientConnection
import io.quarkus.websockets.next.WebSocketConnector
import io.quarkus.websockets.next.runtime.WebSocketClientRecorder.ClientEndpoint
import io.restassured.RestAssured
import jakarta.inject.Inject
import org.jboss.logging.Logger
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.concurrent.TimeUnit


/**
 * https://loopstudy.tistory.com/427
 */
@QuarkusTest
class UnitTest {

  @Inject
  lateinit var log: Logger

  @Inject
  lateinit var connector: WebSocketConnector<CmfyWbskClnt>

  @Inject
  lateinit var cmfyHostChckDao: CmfyHostChckDao


  @Test
  fun testWebPageEndpoint() {
    val res = RestAssured.given()
      .`when`().get("/view/status").then()
      .statusCode(200).log().all().extract()
    log.debug("testWebPageEndpoint res: $res")
  }

  @Test
  fun testRestEndpoint() {
    val res = RestAssured.given()
      .`when`().get("/history/test-prompt-id").then()
      .statusCode(200).log().all().extract()
    log.debug("testRestEndpoint res: $res")
  }

//  @Test
  fun testWebsocketEndpoint() {
    val testClientId = "test-client-id"
    var uri: URI = cmfyHostChckDao.getWebsocketUri(0, testClientId)
    val connection: WebSocketClientConnection = connector
      .addHeader("Authorization", cmfyHostChckDao.getCmfyAuthHead())
      .baseUri(uri).pathParam("clientId", testClientId)
      .connectAndAwait()
    log.debug("testWebsocketEndpoint")
    // Ping messages are sent automatically
//    Assertions.assertTrue(ClientEndpoint.await(5, TimeUnit.SECONDS));
  }

}