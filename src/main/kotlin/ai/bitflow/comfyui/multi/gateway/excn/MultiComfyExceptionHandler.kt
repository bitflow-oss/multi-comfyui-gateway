package ai.bitflow.comfyui.multi.gateway.excn

import ai.bitflow.comfyui.multi.gateway.rsps.ComnRsps
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.apache.hc.core5.http.HttpStatus
import org.jboss.logging.Logger
import java.io.Serializable

@Provider
class MultiComfyExceptionHandler : ExceptionMapper<NodeAndQueEctn> {

  @Inject
  lateinit var log: Logger

  override fun toResponse(e: NodeAndQueEctn): Response {

    var content: ComnRsps<Unit>? = null
    if (e is FullQueEctn) {
      content = ComnRsps(
        code = HttpStatus.SC_NOT_ACCEPTABLE,
        msg = "ComfyUi queues are full")
    } else if (e is NoNodeEctn) {
      content = ComnRsps(
        code = HttpStatus.SC_NOT_ACCEPTABLE,
        msg = "No ComfyUi nodes found")
    }

    log.error("[ComfyExtn]: ${e.javaClass}, message: ${e.message}")
    return Response.status(HttpStatus.SC_OK).entity(content).build()
  }

}

open class NodeAndQueEctn : RuntimeException, Serializable {
  constructor()
}

class FullQueEctn : NodeAndQueEctn {
  constructor()
}

class NoNodeEctn : NodeAndQueEctn {
  constructor()
}
