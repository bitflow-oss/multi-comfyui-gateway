package ai.bitflow.comfyui.api.bridge.rsps

import com.fasterxml.jackson.annotation.JsonProperty

class GnrtTextToImgRsps {

  constructor(clientId: String, stat: String): super() {
    this.clientId = clientId
    this.stat = stat
  }

  @JsonProperty("client_id")
  val clientId: String
  val stat: String
  var errMsg: String? = null

  var que: QueStatRsps? = null

}
