package ai.bitflow.comfyui.multi.gateway.srvc

import ai.bitflow.comfyui.multi.gateway.cnst.WbskCnst
import ai.bitflow.comfyui.multi.gateway.dao.CmfyRestClnt
import ai.bitflow.comfyui.multi.gateway.dao.CmfyWbskClnt
import ai.bitflow.comfyui.multi.gateway.dao.HostJsonTpltLctr
import ai.bitflow.comfyui.multi.gateway.data.CmfyQueInfo
import ai.bitflow.comfyui.multi.gateway.data.CmfyTaskQue
import ai.bitflow.comfyui.multi.gateway.extn.FullQueExtn
import ai.bitflow.comfyui.multi.gateway.rqst.GnrtTextToImgRqst
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

  @Inject
  lateinit var connector: WebSocketConnector<CmfyWbskClnt>

  @ConfigProperty(name = "quarkus.rest-client.comfy-api-1.url")
  lateinit var CMFY_API_HOST_1: String

  @ConfigProperty(name = "quarkus.rest-client.comfy-api-2.url")
  lateinit var CMFY_API_HOST_2: String

  @ConfigProperty(name = "quarkus.rest-client.comfy-api-3.url")
  lateinit var CMFY_API_HOST_3: String

  @ConfigProperty(name = "quarkus.rest-client.comfy-api-4.url")
  lateinit var CMFY_API_HOST_4: String

  @ConfigProperty(name = "comfyui.api.key")
  lateinit var CMFY_API_KEY: String

  @ConfigProperty(name = "comfyui.max.queue.size")
  lateinit var COMFYUI_MAX_QUEUE_SIZE: String

  @Inject
  lateinit var templateEngine: Engine

//  @Inject
//  @Location("wkfw/flux1DevIniv.json")
//  lateinit var flux1DevIniv: Template

  @Inject
  lateinit var hostJsonTpltLctr: HostJsonTpltLctr

  private var taskQue = CmfyTaskQue()


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
  fun generateImages(param: GnrtTextToImgRqst): GnrtTextToImgRsps {
    // find less queue address, but if all are > 2 no queue return null
    val que: CmfyQueInfo = getBestQue()
    if (que.queNo < 1 || que.queNo > 4) {
      throw FullQueExtn()
    }
    val cmfyClnt: CmfyRestClnt = getRestClient(que.queNo)
    // 1. 워크플로우 template에 동적 파라미터 매핑
    var data = param.prompt
//    hostJsonTpltLctr.locate("/'workflow/some-workflow.json")
    param.prompt = templateEngine.getTemplate("/'workflow/some-workflow.json").data("data", data).render()
    log.debug("prompt ${param.prompt}")
    // 2. Rest 생성요청 큐잉
    cmfyClnt.queuePrompt(getCmfyAuthHead(), param)
    // 3. Websocket => nedd to open to listen progress
    cnntCmfyAndSendMsg(que.queNo, param.clientId)
    var ret = GnrtTextToImgRsps(
      clientId = param.clientId,
      stat = WbskCnst.ON_QUE_ADD
    )
    return ret
  }



  /**
   * API를 통한 ComfyUI 워크플로 호스팅
   * https://9elements.com/blog/hosting-a-comfyui-workflow-via-api/
   */
  fun getQueStat(): QueStatRsps {

    var ret = QueStatRsps(
      que1 = taskQue.get1.size,
      que2 = taskQue.get2.size,
      que3 = taskQue.get3.size,
      que4 = taskQue.get4.size
    )
    ret.totlQueCnt = ret.que1 + ret.que2 + ret.que3 + ret.que4
    ret.avilQueCnt = 8 - ret.que1 - ret.que2 - ret.que3 - ret.que4
    return ret
  }

  private fun getBestQue(): CmfyQueInfo {
    val queue = listOf(taskQue.get1, taskQue.get2, taskQue.get3, taskQue.get4)
    val minQueIdx = queue.indexOf(queue.minBy { it.size })
    if (queue[minQueIdx].size < COMFYUI_MAX_QUEUE_SIZE.toInt()) {
      return CmfyQueInfo((minQueIdx + 1), queue[minQueIdx][queue[minQueIdx].size])
    } else {
      return CmfyQueInfo(-1, null)
    }
  }

  fun cnntCmfyAndSendMsg(queNo: Int, clientId: String) {

    var uri: URI? = null
    if (queNo > 0 && queNo < 5) {
      uri = getWebsocketUri(queNo, clientId)
    } else {
      throw FullQueExtn()
    }

    val connection: WebSocketClientConnection = connector
      .addHeader("Authorization", getCmfyAuthHead())
      .baseUri(uri).pathParam("clientId", clientId)
      .connectAndAwait()
    connection.sendTextAndAwait("Hi!")
  }

  private fun getCmfyAuthHead(): String {
    return "Bearer $CMFY_API_KEY"
  }

  private fun getRestClient(cmfyHostIdx: Int): CmfyRestClnt {
    return RestClientBuilder.newBuilder()
      .baseUri(URI("http://${getHostName(cmfyHostIdx)}"))
      .build<CmfyRestClnt>(CmfyRestClnt::class.java)
  }

  private fun getWebsocketUri(cmfyHostIdx: Int, clientId: String): URI {
    return URI.create("ws://${getHostName(cmfyHostIdx)}/ws?clientId=$clientId")
  }

  private fun getHostName(cmfyHostIdx: Int): String {
    if (cmfyHostIdx == 1) {
      return CMFY_API_HOST_1
    } else if (cmfyHostIdx == 2) {
      return CMFY_API_HOST_2
    } else if (cmfyHostIdx == 3) {
      return CMFY_API_HOST_3
    } else if (cmfyHostIdx == 4) {
      return CMFY_API_HOST_4
    } else {
      throw FullQueExtn()
    }
  }

}