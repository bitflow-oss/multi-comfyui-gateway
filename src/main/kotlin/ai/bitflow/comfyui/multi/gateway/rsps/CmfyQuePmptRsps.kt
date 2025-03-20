package ai.bitflow.comfyui.multi.gateway.rsps

import com.fasterxml.jackson.annotation.JsonProperty

data class CmfyQuePmptRsps (
  @JsonProperty("prompt_id")
  var promptId: CmfyQueRman,
  var number: Int,
  @JsonProperty("node_errors")
  var nodeErrors: Map<String, Any>
)
