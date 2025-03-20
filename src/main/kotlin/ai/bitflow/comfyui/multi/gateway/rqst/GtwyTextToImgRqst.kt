package ai.bitflow.comfyui.multi.gateway.rqst

import com.google.gson.internal.LinkedTreeMap

data class GtwyTextToImgRqst(
  var prompt: LinkedTreeMap<String, Any>,
//  @JsonProperty("client_id")
  var clientId: String
)
