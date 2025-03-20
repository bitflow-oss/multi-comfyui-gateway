package ai.bitflow.comfyui.multi.gateway.data

/**
 * Data Structures in Kotlin: Stack & Queue — [PartV]
 * https://medium.com/wearejaya/data-structures-in-kotlin-stack-queue-partv-31771dafa89
 */
class CmfyNodeQue(size: Int) {
  var no: Array<CmfyQue?>? = null
  init {
    this.no = Array<CmfyQue?>(size) { null }
  }
}

data class CmfyTaskItem(
  var nodeIdx: Int = -1,
  var pmpt: String,
  var clntId: String,
  var taskStat: String,
  var imgUrl: String? = null,
  var errMsg: String? = null,
)

class CmfyQue {
  var que = ArrayDeque<CmfyTaskItem>()
}

data class CmfyQueInfo (
  var nodeIdx: Int = -1,
  var queSize: Int = -1
)