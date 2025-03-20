package ai.bitflow.comfyui.multi.gateway.rsps

import com.google.gson.annotations.SerializedName

data class CmfyGetQueRsps (
  @SerializedName("exec_info")
  var execInfo: CmfyQueRman
)

data class CmfyQueRman (
  @SerializedName("queue_remaining")
  var queueRemaining: Int = -1
)