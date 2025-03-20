package ai.bitflow.comfyui.multi.gateway.extn

import ai.bitflow.comfyui.multi.gateway.rsps.ComnRsps
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.apache.hc.core5.http.HttpStatus
import org.jboss.logging.Logger
import java.io.Serializable

@Provider
class MultiComfyExceptionHandler : ExceptionMapper<NodeAndQueExtn> {

  @Inject
  lateinit var log: Logger

  override fun toResponse(e: NodeAndQueExtn): Response {

    var content: ComnRsps<Unit>? = null
    if (e is FullNodeAndQueExtn) {
      content = ComnRsps(
        code = HttpStatus.SC_NOT_ACCEPTABLE,
        msg = "ComfyUi queues are full")
    } else if (e is NoNodeExtnNodeAnd) {
      content = ComnRsps(
        code = HttpStatus.SC_NOT_ACCEPTABLE,
        msg = "No ComfyUi nodes found")
    }

    log.error("[FullQueExtn]: ${e.javaClass}, message: ${e.message}")
    return Response.status(HttpStatus.SC_OK).entity(content).build()
  }

}

open class NodeAndQueExtn : RuntimeException, Serializable {
  constructor()
}

class FullNodeAndQueExtn : NodeAndQueExtn {
  constructor()
}

class NoNodeExtnNodeAnd : NodeAndQueExtn {
  constructor()
}
