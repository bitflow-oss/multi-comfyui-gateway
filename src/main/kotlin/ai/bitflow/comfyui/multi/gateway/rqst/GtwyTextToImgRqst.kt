package ai.bitflow.comfyui.multi.gateway.rqst

import com.google.gson.internal.LinkedTreeMap

data class GtwyTextToImgRqst(
  var prompt: LinkedTreeMap<String, Any>? = null,
  var promptString: String?,
  var clientId: String,
  var seed: String?
)
