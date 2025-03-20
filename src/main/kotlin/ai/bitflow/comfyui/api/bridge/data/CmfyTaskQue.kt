package ai.bitflow.comfyui.api.bridge.data

class CmfyTaskQue {
  var get1 = mutableListOf<MutableMap<String, CmfyTaskItem>>()
  var get2 = mutableListOf<MutableMap<String, CmfyTaskItem>>()
  var get3 = mutableListOf<MutableMap<String, CmfyTaskItem>>()
  var get4 = mutableListOf<MutableMap<String, CmfyTaskItem>>()
  var get5 = mutableListOf<MutableMap<String, CmfyTaskItem>>()
  var get6 = mutableListOf<MutableMap<String, CmfyTaskItem>>()
  var get7 = mutableListOf<MutableMap<String, CmfyTaskItem>>()
  var get8 = mutableListOf<MutableMap<String, CmfyTaskItem>>()
}

data class CmfyTaskItem(
  var fngrPrnt: String,
  var taskStat: String,
  var imgUrl: String? = null,
  var errMsg: String? = null
)

data class CmfyQueInfo(
  var queNo: Int = 0,
  var queItem: MutableMap<String, CmfyTaskItem>? = mutableMapOf()
)