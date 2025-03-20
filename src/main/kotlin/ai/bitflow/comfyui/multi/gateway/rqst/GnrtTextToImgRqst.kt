package ai.bitflow.comfyui.multi.gateway.rqst

import com.fasterxml.jackson.annotation.JsonProperty

data class GnrtTextToImgRqst(
  var prompt: String,
  @JsonProperty("client_id")
  val clientId: String
)
