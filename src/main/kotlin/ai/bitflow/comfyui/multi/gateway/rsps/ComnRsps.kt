package ai.bitflow.comfyui.multi.gateway.rsps

import org.apache.hc.core5.http.HttpStatus

open class ComnRsps<T>(var rslt: T?) {
  constructor () : this(null)
  constructor (code: Int, msg: String?) : this() {
    this.code = code
    this.msg = msg
  }
  var code: Int = HttpStatus.SC_OK
  var msg: String? = null
}
