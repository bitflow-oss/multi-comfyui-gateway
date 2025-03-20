package ai.bitflow.comfyui.multi.gateway.rqst

import com.fasterxml.jackson.annotation.JsonProperty

data class CmfyTextToImgRqst(
  var prompt: String,
  @JsonProperty("client_id")
  var clientId: String
)
