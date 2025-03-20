import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Test

@QuarkusTest
class UnitTest {
  @Test
  fun testWebPageEndpoint() {
    RestAssured.given()
      .`when`().get("/view/status").then()
      .statusCode(200)
//      .body(CoreMatchers.`is`<String?>("Hello from Quarkus REST"))
  }
}