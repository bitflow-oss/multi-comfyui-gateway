package ai.bitflow.comfyui.multi.gateway.dao

import ai.bitflow.comfyui.multi.gateway.cnst.WbskCnst
import ai.bitflow.comfyui.multi.gateway.extn.FullQueExtn
import ai.bitflow.comfyui.multi.gateway.rsps.GnrtTextToImgRsps
import ai.bitflow.comfyui.multi.gateway.srvc.GnrtJobSrvc
import io.quarkus.websockets.next.*
import io.vertx.core.buffer.Buffer
import jakarta.inject.Inject
import org.jboss.logging.Logger


/**
 * 게이트웨이 웹소켓 서버에서 ComfyUI 도커 4대로 요청 분배하여 전송하는 클라이언트
 */
@WebSocketClient(path = "/ws/?clientId={clientId}")
class CmfyWbskClnt {

  @Inject
  lateinit var log: Logger

  @Inject
  lateinit var gnrtJobSrvc: GnrtJobSrvc

  @OnPingMessage
  fun onPingMessage(data: Buffer) {
    log.debug("[WBSK-CLNT] onPing")
  }

  @OnPongMessage
  fun onPongMessage(data: Buffer) {
    log.debug("[WBSK-CLNT] onPong")
  }

  @OnOpen
  fun onOpen() {
    log.debug("[WBSK-CLNT] onOpen")
  }

  @OnClose
  fun onClose() {
    log.debug("[WBSK-CLNT] onClose")
  }

  @OnError
  fun onError(e: RuntimeException) {

    log.debug("[WBSK-CLNT] onError => ${e.message}")

    if (e is FullQueExtn) {
      log.warn("[WBSK-CLNT] Waiting linea are full")
      val ret1 = GnrtTextToImgRsps(
        clientId = "",
        stat = WbskCnst.ON_QUE_FULL
      )
      ret1.errMsg = e.message ?: "Waiting linea are full"
    }
    e.printStackTrace()
  }

  @OnTextMessage
  fun onTextMessage(param: GnrtTextToImgRsps) {
    log.debug("[WBSK-CLNT] onMessage => $param")
  }

  @OnBinaryMessage
  fun onBinaryMessage(msg: Buffer) {
    log.debug("[WBSK-CLNT] onBinaryMessage")
  }

}