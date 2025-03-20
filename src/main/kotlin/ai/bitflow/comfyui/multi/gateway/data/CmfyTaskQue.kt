package ai.bitflow.comfyui.multi.gateway.data

class CmfyTaskQue {
  /**
   * Data Structures in Kotlin: Stack & Queue — [PartV]
   * https://medium.com/wearejaya/data-structures-in-kotlin-stack-queue-partv-31771dafa89
   */
  var get = arrayOf<ArrayDeque<MutableMap<String, CmfyTaskItem>>>()
}

data class CmfyTaskItem(
  var fngrPrnt: String,
  var taskStat: String,
  var imgUrl: String? = null,
  var errMsg: String? = null
)

data class CmfyQueInfo(
  var queNo: Int = -1,
  var queItem: MutableMap<String, CmfyTaskItem>? = mutableMapOf()
)