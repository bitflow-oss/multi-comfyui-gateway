package ai.bitflow.comfyui.multi.gateway.endp

import ai.bitflow.comfyui.multi.gateway.cnst.WbskCnst
import ai.bitflow.comfyui.multi.gateway.excn.FullQueEctn
import ai.bitflow.comfyui.multi.gateway.rqst.GtwyTextToImgRqst
import ai.bitflow.comfyui.multi.gateway.rsps.GnrtTextToImgRsps
import ai.bitflow.comfyui.multi.gateway.srvc.GnrtJobSrvc
import io.quarkus.websockets.next.*
import io.vertx.core.buffer.Buffer
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger

/**
 * 외부에서 이미지 생성을 위해 웹소켓 API로 접속하는 서버
 * @SessionScoped => 이 서버 엔드포인트는 공유되지 않으며 세션/연결로 범위가 지정됩니다.
 * @RequestScoped => 이 서버 엔드포인트는 각 콜백 메서드 실행에 대해 인스턴스화됩니다.
 */
//@SessionScoped
//@RolesAllowed(*[JwtAuth.SIGN_IN])
@WebSocket(path = "/ws/?clientId={clientId}")
@ApplicationScoped
class WebSockEndp {

  @Inject
  lateinit var log: Logger

  @Inject
  lateinit var connection: WebSocketConnection

  @Inject
  lateinit var gnrtJobSrvc: GnrtJobSrvc


  @OnPingMessage
  fun onPingMessage(data: Buffer) {
  }

  @OnPongMessage
  fun onPongMessage(data: Buffer) {
  }

  @OnOpen
  fun onOpen(conn: WebSocketConnection, @PathParam clientId: String): GnrtTextToImgRsps {
    log.debug("[WBSK] onOpen clientId / conn => $clientId / $conn")
    var ret = GnrtTextToImgRsps(
      clientId = conn.id(),
      stat = WbskCnst.ON_CONNECT
    )
    ret.que = gnrtJobSrvc.getQueStat()
    return ret
  }

  @OnClose
  fun onClose(conn: WebSocketConnection, @PathParam clientId: String) {
    log.debug("[WBSK] onClose clientId / conn => ${getClientId(conn)} / $conn")
//    return GnrtTextToImgRsps(
//      clientId = conn.id(),
//      stat = WbskCnst.ON_DISCONNECTED
//    )
  }

  @OnError
  fun onError(@PathParam clientId: String, e: RuntimeException, conn: WebSocketConnection): GnrtTextToImgRsps {

    log.debug("[WBSK] onError clientId / errMsg => ${getClientId(conn)} / ${e.message}")

    if (e is FullQueEctn) {
      log.warn("[WBSK] Waiting linea are full")
      val ret1 = GnrtTextToImgRsps(
        clientId = getClientId(conn),
        stat = WbskCnst.ON_QUE_FULL
      )
      ret1.errMsg = e.message ?: "Waiting linea are full"
      return ret1
    }

    e.printStackTrace()
    val ret = GnrtTextToImgRsps(
      clientId = conn.id(),
      stat = WbskCnst.ON_ERROR
    )
    ret.errMsg = e.message ?: "Unknown error"
    return ret
  }

  @OnTextMessage
  fun onTextMessage(@PathParam clientId: String, param: GtwyTextToImgRqst, conn: WebSocketConnection): String {
    log.debug("[WBSK] onMessage param / clientId / conn => $param / $clientId / $conn")
    val queStat = gnrtJobSrvc.getQueStat()
    val addSucc = gnrtJobSrvc.generateImages(param)
    connection.broadcast().sendText(queStat)
    return addSucc.toString()
  }

  @OnBinaryMessage
  fun onBinaryMessage(@PathParam clientId: String, msg: Buffer, conn: WebSocketConnection) {
    log.debug("[WBSK] onBinaryMessage clientId => clientId")
  }

  fun getClientId(conn: WebSocketConnection): String {
    return conn.pathParam("clientId")
  }

}