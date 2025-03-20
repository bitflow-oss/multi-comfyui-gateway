package ai.bitflow.comfyui.multi.gateway.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject


data class CmfyTextToImgData(
  @Serializable
  var prompt: JsonObject? = null,
  @Serializable
  var client_id: String,
  var nodeIdx: Int = -1,
  var taskStat: String? = null
)
