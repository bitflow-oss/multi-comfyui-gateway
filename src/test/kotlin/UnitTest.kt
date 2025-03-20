import ai.bitflow.comfyui.multi.gateway.dao.CmfyWbskClnt
import ai.bitflow.comfyui.multi.gateway.srvc.GnrtJobSrvc
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.websockets.next.WebSocketClientConnection
import io.quarkus.websockets.next.WebSocketConnector
import io.quarkus.websockets.next.runtime.WebSocketClientRecorder.ClientEndpoint
import io.restassured.RestAssured
import jakarta.inject.Inject
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.concurrent.TimeUnit


/**
 * https://loopstudy.tistory.com/427
 */
@QuarkusTest
class UnitTest {

  @Inject
  lateinit var connector: WebSocketConnector<CmfyWbskClnt>

  @Inject
  lateinit var gnrtJobSrvc: GnrtJobSrvc


  @Test
  fun testWebPageEndpoint() {
    RestAssured.given()
      .`when`().get("/view/status").then()
      .statusCode(200).log().all().extract()
  }

  @Test
  fun testRestEndpoint() {
    RestAssured.given()
      .`when`().get("/history/test-prompt-id").then()
      .statusCode(200).log().all().extract()
  }

  @Test
  fun testWebsocketEndpoint() {
    val testClientId = "test-client-id"
    var uri: URI = gnrtJobSrvc.getWebsocketUri(0, testClientId)
    val connection: WebSocketClientConnection = connector
      .addHeader("Authorization", gnrtJobSrvc.getCmfyAuthHead())
      .baseUri(uri).pathParam("clientId", testClientId)
      .connectAndAwait()
    // Ping messages are sent automatically
//    assertTrue(ClientEndpoint.PONG.await(5, TimeUnit.SECONDS));
  }

}