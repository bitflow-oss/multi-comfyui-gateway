package ai.bitflow.comfyui.multi.gateway.dao

import ai.bitflow.comfyui.multi.gateway.rqst.CmfyTextToImgRqst
import ai.bitflow.comfyui.multi.gateway.rqst.CmfyUpldImgRqst
import ai.bitflow.comfyui.multi.gateway.rsps.CmfyGetQueRsps
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery


/**
 * ComfyUI API 사용법 구축
 * https://youngri.tistory.com/40
 */
@RegisterRestClient
interface CmfyRestClnt {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("prompt")
  fun getQueuePrompt(@HeaderParam("Authorization") authorization: String): CmfyGetQueRsps

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("prompt")
  fun queuePrompt(@HeaderParam("Authorization") authorization: String
                  , param: CmfyTextToImgRqst): Map<String, Any>

  /**
   * Check Generation Progress (Polling)
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/history/{promptId}")
  fun getHistory(@HeaderParam("Authorization") authorization: String
                 , @RestPath promptId: String): String

  /**
   * Todo: Check response type => b64 or binary
   */
  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Path("view")
  fun getImage(@HeaderParam("Authorization") authorization: String
               , @RestQuery filename: String, @RestQuery subfolder: String
               , @RestQuery type: String): Response

  /**
   * Todo: Check : response type is not clear
   */
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Path("upload/image")
  fun uploadImage(@HeaderParam("Authorization") authorization: String
                  , param: CmfyUpldImgRqst): Response

}