package ai.bitflow.comfyui.api.bridge.rqst

import com.fasterxml.jackson.annotation.JsonProperty

data class GnrtTextToImgRqst(
  var prompt: String,
  @JsonProperty("client_id")
  val clientId: String
)
