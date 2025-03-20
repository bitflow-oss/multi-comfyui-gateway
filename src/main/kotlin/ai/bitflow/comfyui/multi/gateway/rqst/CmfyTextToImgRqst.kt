package ai.bitflow.comfyui.multi.gateway.rqst

import org.jose4j.json.internal.json_simple.JSONObject


data class CmfyTextToImgRqst(
  var prompt: JSONObject? = null,
  var client_id: String,
  var nodeIdx: Int = -1,
  var taskStat: String? = null
)
