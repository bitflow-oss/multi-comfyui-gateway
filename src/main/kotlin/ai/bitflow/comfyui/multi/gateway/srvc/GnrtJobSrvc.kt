package ai.bitflow.comfyui.multi.gateway.srvc

import ai.bitflow.comfyui.multi.gateway.cnst.TaskStat
import ai.bitflow.comfyui.multi.gateway.cnst.WbskCnst
import ai.bitflow.comfyui.multi.gateway.dao.CmfyHostChckDao
import ai.bitflow.comfyui.multi.gateway.dao.CmfyRestClnt
import ai.bitflow.comfyui.multi.gateway.dao.CmfyWbskClnt
import ai.bitflow.comfyui.multi.gateway.dao.HostJsonTpltLctr
import ai.bitflow.comfyui.multi.gateway.data.CmfyQueInfo
import ai.bitflow.comfyui.multi.gateway.data.CmfyTaskItem
import ai.bitflow.comfyui.multi.gateway.excn.NoNodeEctn
import ai.bitflow.comfyui.multi.gateway.rqst.CmfyTextToImgRqst
import ai.bitflow.comfyui.multi.gateway.rqst.GtwyTextToImgRqst
import ai.bitflow.comfyui.multi.gateway.rsps.GnrtTextToImgRsps
import ai.bitflow.comfyui.multi.gateway.rsps.QueStatRsps
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.Gson
import io.quarkus.qute.Engine
import io.quarkus.websockets.next.WebSocketClientConnection
import io.quarkus.websockets.next.WebSocketConnector
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.net.URI


@ApplicationScoped
class GnrtJobSrvc {

  @Inject
  lateinit var log: Logger

  @ConfigProperty(name = "comfyui.max.queue.size.each")
  lateinit var COMFYUI_MAX_QUE_SIZE_EACH: String

  @Inject
  lateinit var templateEngine: Engine

  @Inject
  lateinit var cmfyHostChckDao: CmfyHostChckDao

  @Inject
  lateinit var hostJsonTpltLctr: HostJsonTpltLctr

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
  fun generateImages(inParam: GtwyTextToImgRqst): Boolean {
    val jsonStrLoad = templateEngine.getTemplate("./workflow/some-workflow.json").data("data", "").render()
    log.debug("[generateImages][jsonStrLoad] $jsonStrLoad")
    val rqstParam = CmfyTextToImgRqst(
      prompt = templateEngine.getTemplate("./workflow/some-workflow.json").data("data", "").render(),
      clientId = inParam.clientId,
      nodeIdx = -1,
      taskStat = TaskStat.REQUESTED
    )
    log.debug("[CmfyUiQueRqst] " + Gson().toJson(rqstParam))
    return cmfyHostChckDao.addCmfyTaskQue(rqstParam)
  }

  /**
   * API를 통한 ComfyUI 워크플로 호스팅
   * https://9elements.com/blog/hosting-a-comfyui-workflow-via-api/
   */
  fun getQueStat(): QueStatRsps? {

    val queStat: List<ArrayDeque<CmfyTextToImgRqst>> = cmfyHostChckDao.getQueStat() ?: return null
    val queSize = mutableListOf<Int>()
    var totlQueUse = 0
    queStat.forEach { it ->
      queSize.add(it.size)
      totlQueUse += it.size
    }
    return QueStatRsps (
      avilNodeCnt = queStat.size,
      queSize = queSize,
      totlQueCnt = queStat.size.times(COMFYUI_MAX_QUE_SIZE_EACH.toInt()),
      avilQueCnt = queStat.size.times(COMFYUI_MAX_QUE_SIZE_EACH.toInt()) - totlQueUse
    )
  }

}