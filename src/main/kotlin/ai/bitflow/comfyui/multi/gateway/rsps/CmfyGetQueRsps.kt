package ai.bitflow.comfyui.multi.gateway.rsps

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

data class CmfyGetQueRsps (
  @JsonProperty("exec_info")
  var execInfo: CmfyQueRman
)

data class CmfyQueRman (
  @JsonProperty("queue_remaining")
  var queueRemaining: Int = -1
)