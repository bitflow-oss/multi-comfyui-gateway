package ai.bitflow.comfyui.multi.gateway.rsps

data class QueStatRsps (
  var avilNodeCnt: Int,
  var queSize: List<Int>,
  var totlQueCnt: Int,
  var avilQueCnt: Int
)