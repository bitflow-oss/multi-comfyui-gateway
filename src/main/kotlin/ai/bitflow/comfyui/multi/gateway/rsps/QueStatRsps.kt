package ai.bitflow.comfyui.multi.gateway.rsps

class QueStatRsps {

  constructor(
    queSize: Int,
  ): super() {
    this.queSize = queSize
    this.que = Array(queSize) { -1 }
  }

//  constructor(
//    queSize: Int = -1,
//    que: Array<Int>,
//    totlQueCnt: Int,
//    avilQueCnt: Int
//  ): super() {
//    this.queSize = queSize
//    this.que = que
//    this.totlQueCnt = totlQueCnt
//    this.avilQueCnt = avilQueCnt
//  }

  var queSize: Int = -1
  var que: Array<Int> = Array(4) { -1 }
  var totlQueCnt: Int = -1
  var avilQueCnt: Int = -1

}