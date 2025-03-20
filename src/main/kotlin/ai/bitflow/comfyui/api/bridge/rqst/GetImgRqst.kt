package ai.bitflow.comfyui.api.bridge.rqst

import com.fasterxml.jackson.annotation.JsonProperty

data class GetImgRqst(
  val filename: String,
  val subfolder: String,
  @JsonProperty("folder_type")
  val type: String
)
