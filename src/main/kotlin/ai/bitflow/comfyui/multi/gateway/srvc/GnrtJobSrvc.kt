package ai.bitflow.comfyui.multi.gateway.srvc

import ai.bitflow.comfyui.multi.gateway.cnst.WbskCnst
import ai.bitflow.comfyui.multi.gateway.dao.CmfyHostChckDao
import ai.bitflow.comfyui.multi.gateway.dao.CmfyRestClnt
import ai.bitflow.comfyui.multi.gateway.dao.CmfyWbskClnt
import ai.bitflow.comfyui.multi.gateway.dao.HostJsonTpltLctr
import ai.bitflow.comfyui.multi.gateway.data.CmfyQueInfo
import ai.bitflow.comfyui.multi.gateway.data.CmfyTaskQue
import ai.bitflow.comfyui.multi.gateway.excn.CmfyQueEctn
import ai.bitflow.comfyui.multi.gateway.excn.NoNodeEctn
import ai.bitflow.comfyui.multi.gateway.rqst.CmfyTextToImgRqst
import ai.bitflow.comfyui.multi.gateway.rqst.GtwyTextToImgRqst
import ai.bitflow.comfyui.multi.gateway.rsps.GnrtTextToImgRsps
import ai.bitflow.comfyui.multi.gateway.rsps.QueStatRsps
import io.quarkus.qute.Engine
import io.quarkus.websockets.next.WebSocketClientConnection
import io.quarkus.websockets.next.WebSocketConnector
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.RestClientBuilder
import org.jboss.logging.Logger
import java.net.URI


@ApplicationScoped
class GnrtJobSrvc {

  @Inject
  lateinit var log: Logger

  @ConfigProperty(name = "comfyui.max.queue.size.each")
  lateinit var COMFYUI_MAX_QUE_SIZE_EACH: String

  @Inject
  lateinit var connector: WebSocketConnector<CmfyWbskClnt>

  @Inject
  lateinit var templateEngine: Engine

  @Inject
  lateinit var hostJsonTpltLctr: HostJsonTpltLctr

  @Inject
  lateinit var cmfyHostChckDao: CmfyHostChckDao

  private var comfyUiQues: CmfyTaskQue? = null


  /**
   * API를 통한 ComfyUI 워크플로 호스팅
   * https://9elements.com/blog/hosting-a-comfyui-workflow-via-api/
   *
   * @routes.get('/ws') ⇒ WebSocket 객체를 반환하고 상태 및 실행 메시지를 보냅니다.
   * @routes.post("/prompt") ⇒ 워크플로에 대한 대기열 프롬프트, prompt_id 또는 오류 반환
   * @routes.get("/history/{prompt_id}") ⇒ 주어진 prompt_id에 대한 큐 또는 출력을 반환합니다.
   * @routes.get("/view") ⇒ 파일 이름, 하위 폴더, 유형("input", "output", "temp")이 주어진 이미지를 반환합니다.
   * @routes.post("/upload/image")⇒ image_data와 type("input", "output", "temp")을 지정하여 ComfyUI에 이미지를 업로드합니다.
   *
   * 1. 생성 큐잉 : rest / post / prompt
   * 2. 생성 히스토리 : rest / get / history
   * 3. 이미지 검색 : rest / get / view
   * 4. 이미지 업로드 : rest / post / upload/image
   *
   * 순서
   * 1. ComfyUI에 WebSocket 연결 설정
   * 2. API 호출을 통해 프롬프트 대기
   * 3. WebSocket 연결을 사용하여 프롬프트 진행 상황 추적
   * 4. 프롬프트에 대해 생성된 이미지를 가져옵니다.
   * 5. 이미지를 로컬에 저장
   */
  fun generateImages(inParam: GtwyTextToImgRqst): GnrtTextToImgRsps {

    val que: CmfyQueInfo? = getBestQue()
    if (que == null) {
      throw NoNodeEctn()
    }
    val cmfyClnt: CmfyRestClnt = cmfyHostChckDao.getRestClient(que.queNo)
    // 1. 워크플로우 template에 동적 파라미터 매핑
//    var data = param.prompt
//    hostJsonTpltLctr.locate("/'workflow/some-workflow.json")
    var outParam = CmfyTextToImgRqst(
      prompt = templateEngine.getTemplate("/'workflow/some-workflow.json").data("data", "").render(),
      clientId = inParam.clientId
    )
    log.debug("prompt ${outParam.prompt}")
    // 2. Rest 생성요청 큐잉
    cmfyClnt.queuePrompt(cmfyHostChckDao.getCmfyAuthHead(), outParam)
    // 3. Websocket => nedd to open to listen progress
    cnntCmfyAndSendMsg(que.queNo, outParam.clientId)
    var ret = GnrtTextToImgRsps(
      clientId = outParam.clientId,
      stat = WbskCnst.ON_QUE_ADD
    )
    return ret
  }

  /**
   * API를 통한 ComfyUI 워크플로 호스팅
   * https://9elements.com/blog/hosting-a-comfyui-workflow-via-api/
   */
  fun getQueStat(): QueStatRsps? {
    if (comfyUiQues == null || comfyUiQues!!.get.size < 1) {
      return null
    }
    val size = comfyUiQues!!.get.size
    var ret = QueStatRsps(size)
    var totlCnt = 0
    ret.que.forEachIndexed { idx, it ->
      ret.que[idx] = comfyUiQues!!.get[idx].size
      totlCnt += comfyUiQues!!.get[idx].size
    }
    ret.totlQueCnt = totlCnt
    ret.avilQueCnt = comfyUiQues!!.get.size.times(COMFYUI_MAX_QUE_SIZE_EACH.toInt()) - totlCnt
    return ret
  }

  private fun getBestQue(): CmfyQueInfo? {

    if (cmfyHostChckDao.getComfyHostSize() < 1) {
      cmfyHostChckDao.getComfyHost()
    }
    if (comfyUiQues == null || comfyUiQues!!.get.size < 1) {
      return null
    }
    val minQueIdx = comfyUiQues!!.get.indexOf(comfyUiQues!!.get.minBy { it.size })
    if (comfyUiQues!!.get[minQueIdx].size < COMFYUI_MAX_QUE_SIZE_EACH.toInt()) {
      return CmfyQueInfo(
        (minQueIdx + 1),
        comfyUiQues!!.get[minQueIdx][comfyUiQues!!.get[minQueIdx].size]
      )
    } else {
      throw CmfyQueEctn()
    }
  }

  fun cnntCmfyAndSendMsg(queNo: Int, clientId: String) {
    var uri: URI = getWebsocketUri(queNo, clientId)
    val connection: WebSocketClientConnection = connector
      .addHeader("Authorization", cmfyHostChckDao.getCmfyAuthHead())
      .baseUri(uri).pathParam("clientId", clientId)
      .connectAndAwait()
    connection.sendTextAndAwait("Hi!")
  }

  fun getWebsocketUri(idx: Int, clientId: String): URI {
    return URI.create("ws://${cmfyHostChckDao.getComfyHostNameAt(idx)}/ws?clientId=$clientId")
  }

}