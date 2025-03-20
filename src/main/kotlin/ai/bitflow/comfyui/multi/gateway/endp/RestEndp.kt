package ai.bitflow.comfyui.multi.gateway.endp

import ai.bitflow.comfyui.multi.gateway.rqst.CmfyUpldImgRqst
import ai.bitflow.comfyui.multi.gateway.rqst.GnrtTextToImgRqst
import ai.bitflow.comfyui.multi.gateway.rsps.ComnRsps
import ai.bitflow.comfyui.multi.gateway.rsps.GnrtTextToImgRsps
import ai.bitflow.comfyui.multi.gateway.srvc.GnrtJobSrvc
import io.vertx.core.http.HttpServerRequest
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.jboss.logging.Logger
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery

@Path("")
class RestEndp {


  @Inject
  lateinit var log: Logger

  @Inject
  lateinit var gnrtJobSrvc: GnrtJobSrvc

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("prompt")
  fun queuePrompt(param: GnrtTextToImgRqst, req: HttpServerRequest)
    : ComnRsps<GnrtTextToImgRsps>? {
    val ret: GnrtTextToImgRsps = gnrtJobSrvc.generateImages(param)
    return ComnRsps(ret)
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/history/{promptId}")
  fun getHistory(@RestPath promptId: String, req: HttpServerRequest)
    : ComnRsps<Boolean>? {
    return ComnRsps(false)
  }

  /**
   * Todo: Check response type => b64 or binary
   */
  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Path("view")
  fun getImage(@RestQuery filename: String, @RestQuery subfolder: String
               , @RestQuery type: String, req: HttpServerRequest)
    : ComnRsps<Boolean>? {
    return ComnRsps(false)
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Path("upload/image")
  fun uploadImage(param: CmfyUpldImgRqst, req: HttpServerRequest)
    : ComnRsps<Boolean>? {
    return ComnRsps(false)
  }

}