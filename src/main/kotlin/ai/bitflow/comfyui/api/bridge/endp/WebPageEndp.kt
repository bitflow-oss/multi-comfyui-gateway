package ai.bitflow.comfyui.api.bridge.endp

import io.quarkus.qute.Location
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.jboss.logging.Logger
import java.util.Objects

@Path("/view")
class WebPageEndp {


  @Inject
  lateinit var log: Logger

  @Inject
  @Location("view/status.html")
  lateinit var statusPage: Template

  @GET
  @Path("status")
  @Produces(MediaType.TEXT_HTML)
  fun getStatusPage(): String {
    return statusPage.data("name", "Quarkus").render()
  }

}