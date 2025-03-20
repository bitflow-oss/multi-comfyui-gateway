package ai.bitflow.comfyui.api.bridge.extn

import ai.bitflow.comfyui.api.bridge.rsps.ComnRsps
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.apache.hc.core5.http.HttpStatus
import org.jboss.logging.Logger
import java.io.Serializable

@Provider
class CustomExceptionhandler : ExceptionMapper<FullQueExtn> {

  @Inject
  lateinit var log: Logger

  override fun toResponse(e: FullQueExtn): Response {
    var content = ComnRsps<Unit>(
      code = HttpStatus.SC_NOT_ACCEPTABLE,
      msg = "ComfyUi Api queue is full")
    log.error("[FullQueExtn]: ${e.javaClass}, message: ${e.message}")
    return Response.status(HttpStatus.SC_OK).entity(content).build()
  }

}

open class FullQueExtn : RuntimeException, Serializable {
  constructor()
}
