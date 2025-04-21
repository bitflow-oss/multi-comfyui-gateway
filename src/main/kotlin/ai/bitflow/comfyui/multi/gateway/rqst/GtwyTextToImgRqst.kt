package ai.bitflow.comfyui.multi.gateway.rqst

import com.google.gson.internal.LinkedTreeMap

data class GtwyTextToImgRqst(
  var prompt: String?,
  var clientId: String,
  var seed: String?
)
