package ai.bitflow.comfyui.multi.gateway.rsps

class QueStatRsps {

  constructor(
    queSize: Int,
  ): super() {
    this.queSize = queSize
    this.que = Array(queSize) { -1 }
  }

  var queSize: Int = -1
  var que: Array<Int> = Array(4) { -1 }
  var totlQueCnt: Int = -1
  var avilQueCnt: Int = -1

}