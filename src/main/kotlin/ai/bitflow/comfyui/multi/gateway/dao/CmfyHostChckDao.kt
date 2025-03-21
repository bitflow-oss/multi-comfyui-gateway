package ai.bitflow.comfyui.multi.gateway.dao

import ai.bitflow.comfyui.multi.gateway.cnst.LaunchEnv
import ai.bitflow.comfyui.multi.gateway.cnst.WbskCnst
import ai.bitflow.comfyui.multi.gateway.data.CmfyQueInfo
import ai.bitflow.comfyui.multi.gateway.excn.FullQueEctn
import ai.bitflow.comfyui.multi.gateway.excn.NoNodeEctn
import ai.bitflow.comfyui.multi.gateway.rqst.CmfyTextToImgRqst
import ai.bitflow.comfyui.multi.gateway.rsps.CmfyGetQueRsps
import ai.bitflow.comfyui.multi.gateway.rsps.CmfyQuePmptRsps
import ai.bitflow.comfyui.multi.gateway.rsps.GnrtTextToImgRsps
import com.google.gson.Gson
import io.quarkus.websockets.next.WebSocketClientConnection
import io.quarkus.websockets.next.WebSocketConnector
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.RestClientBuilder
import org.jboss.logging.Logger
import java.io.File
import java.net.URI
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.toPath

@ApplicationScoped
class CmfyHostChckDao {

  @Inject
  lateinit var log: Logger

  @ConfigProperty(name = "launch.env")
  lateinit var LAUNCH_ENV: String

  @ConfigProperty(name = "comfyui.instance.count")
  lateinit var COMFYUI_INSTANCE_COUNT: String

  @ConfigProperty(name = "quarkus.rest-client.comfyui-1.url")
  lateinit var COMFYI_HOST_1: String

  @ConfigProperty(name = "quarkus.rest-client.comfyui-2.url")
  lateinit var COMFYI_HOST_2: String

  @ConfigProperty(name = "quarkus.rest-client.comfyui-3.url")
  lateinit var COMFYI_HOST_3: String

  @ConfigProperty(name = "quarkus.rest-client.comfyui-4.url")
  lateinit var COMFYI_HOST_4: String

  @ConfigProperty(name = "quarkus.rest-client.comfyui-5.url")
  lateinit var COMFYI_HOST_5: String

  @ConfigProperty(name = "quarkus.rest-client.comfyui-6.url")
  lateinit var COMFYI_HOST_6: String

  @ConfigProperty(name = "quarkus.rest-client.comfyui-7.url")
  lateinit var COMFYI_HOST_7: String

  @ConfigProperty(name = "quarkus.rest-client.comfyui-8.url")
  lateinit var COMFYI_HOST_8: String

  @ConfigProperty(name = "comfyui.api.key")
  lateinit var CMFY_API_KEY: String

  @ConfigProperty(name = "comfyui.max.queue.size.each")
  lateinit var COMFYUI_MAX_QUE_SIZE_EACH: String

  @ConfigProperty(name = "comfyui.max.queue.size.total")
  lateinit var COMFYUI_MAX_QUE_SIZE_TOTL: String

  @Inject
  lateinit var connector: WebSocketConnector<CmfyWbskClnt>

  private var comfyHosts = mutableListOf<String>()

  private var comfyUiQues = mutableListOf<ArrayDeque<CmfyTextToImgRqst>>()

  fun getCmfyAuthHead(): String {
    return "Bearer $CMFY_API_KEY"
  }

  fun getComfyHostList(): MutableList<String> {
    if (this.comfyHosts.size < 1) {
      val cnt = COMFYUI_INSTANCE_COUNT.toInt()
      val ret = arrayOfNulls<String>(cnt)
      for (i in 0..< cnt) {
        when (i) {
          0 -> ret[i] = COMFYI_HOST_1
          1 -> ret[i] = COMFYI_HOST_2
          2 -> ret[i] = COMFYI_HOST_3
          3 -> ret[i] = COMFYI_HOST_4
          4 -> ret[i] = COMFYI_HOST_5
          5 -> ret[i] = COMFYI_HOST_6
          6 -> ret[i] = COMFYI_HOST_7
          7 -> ret[i] = COMFYI_HOST_8
        }
      }
      initChckComfyUiNodeStat(ret)
    }
    return this.comfyHosts
  }

  private fun initChckComfyUiNodeStat(cmfyHost: Array<String?>) {
    log.debug("[ConfigCmfySrvr] count ${cmfyHost.size}")
    cmfyHost.forEachIndexed { i, it ->
      log.debug("[getRestClient:$i] $it")
      val cmfyClnt: CmfyRestClnt = testRestClientWithHostName(it!!)
      try {
        val rsps: CmfyGetQueRsps = cmfyClnt.getQueuePrompt(getCmfyAuthHead())
        // Todo: if succedeed
        log.debug("[GetPmptRsps] " + Gson().toJson(rsps))
        if (rsps.execInfo.queueRemaining != null) {
          this.comfyHosts.add(it)
          this.comfyUiQues.add(ArrayDeque())
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun testRestClientWithHostName(hostName: String): CmfyRestClnt {
    val uri = "http://$hostName"
    log.debug("[RestClientUri] $uri")
    return RestClientBuilder.newBuilder()
      .baseUri(URI(uri))
      .build(CmfyRestClnt::class.java)
  }

  fun getRestClient(idx: Int): CmfyRestClnt {
    return RestClientBuilder.newBuilder()
      .baseUri(URI("http://${getComfyHostNameAt(idx)}"))
      .build(CmfyRestClnt::class.java)
  }

  fun getComfyHostNameAt(idx: Int): String {
    val hostName = getComfyHostList()
    if (idx < 0 || idx >= hostName.size) {
      throw FullQueEctn()
    } else {
      return getComfyHostList()[idx]
    }
  }

  fun getBestQue(task: CmfyTextToImgRqst): ArrayDeque<CmfyTextToImgRqst>? {
    val cmfyHostList = getComfyHostList()
    if (cmfyHostList.isEmpty()) {
      throw NoNodeEctn()
    }
    val minQueIdx = this.comfyUiQues.indexOf(comfyUiQues.minBy { it.size })
    if (this.comfyUiQues[minQueIdx].size < COMFYUI_MAX_QUE_SIZE_EACH.toInt()) {
      task.nodeIdx = minQueIdx
      return this.comfyUiQues[minQueIdx]
    } else {
      return null
    }
  }

  fun getBestQueInfo(): CmfyQueInfo? {
    val cmfyHostList = getComfyHostList()
    if (cmfyHostList.isEmpty()) {
      throw NoNodeEctn()
    }
    val minQueIdx = this.comfyUiQues.indexOf(comfyUiQues.minBy { it.size })
    if (this.comfyUiQues[minQueIdx].size < COMFYUI_MAX_QUE_SIZE_EACH.toInt()) {
      return CmfyQueInfo(
        nodeIdx = minQueIdx,
        queSize = this.comfyUiQues[minQueIdx].size
      )
    }
    return null
  }

  fun pullQue(nodeIdx: Int): CmfyQuePmptRsps? {
    val item: CmfyTextToImgRqst = this.comfyUiQues[nodeIdx].removeFirst()
    val cmfyClnt: CmfyRestClnt = getRestClient(nodeIdx)
    log.debug("[cmfyRqst] $item")
    var cmfyRsps: CmfyQuePmptRsps? = null
    try {
      cmfyRsps = cmfyClnt.queuePrompt(getCmfyAuthHead(), item)
      log.debug("[cmgyRsps] $cmfyRsps")
    } catch (e: Exception) {
      e.printStackTrace()
    }
//    cnntCmfyAndSendMsg(nodeIdx, item.clientId)
    return cmfyRsps
  }

  fun getQueStat(): List<ArrayDeque<CmfyTextToImgRqst>>? {
    return this.comfyUiQues
  }

  fun addCmfyTaskQue(task: CmfyTextToImgRqst): CmfyQuePmptRsps? {
    val que: ArrayDeque<CmfyTextToImgRqst> = getBestQue(task) ?: throw FullQueEctn()
    log.debug("[AddedCmfyQue][nodeIdx:${task.nodeIdx}][queIdx:${que.size}][param:${task.prompt}]")
    que.addLast(task)
    // Todo: 임시,
    return pullQue(task.nodeIdx)
  }

  fun cnntCmfyAndSendMsg(queNo: Int, clientId: String) {
    var uri: URI = getWebsocketUri(queNo, clientId)
    val connection: WebSocketClientConnection = connector
      .addHeader("Authorization", getCmfyAuthHead())
      .baseUri(uri).pathParam("clientId", clientId)
      .connectAndAwait()
    connection.sendTextAndAwait("Hi!")
  }

  fun getWebsocketUri(idx: Int, clientId: String): URI {
    return URI.create("ws://${getComfyHostNameAt(idx)}/ws?clientId=$clientId")
  }

  fun getAppBasePath(): String {
    var path: Path? = null
    when (LAUNCH_ENV) {
      LaunchEnv.DEV -> path =  getClasspathResourceURI()!!.toPath().parent.parent.parent.parent
      LaunchEnv.STAG -> path = Paths.get("")
      LaunchEnv.PROD -> path = Paths.get("")
    }
    return path!!.absolutePathString()
  }

  fun getClasspathResourceURI(): URI? {
    val classLoader = Thread.currentThread().contextClassLoader
    val url: URL? = classLoader.getResource("application.properties")
    return url?.toURI()
  }

  fun test() {
    // 1. 워크플로우 template에 동적 파라미터 매핑
    // 2. Rest 생성요청 큐잉
//    var data = param.prompt
//    hostJsonTpltLctr.locate("/'workflow/some-workflow.json")
//    val cmfyRsps: Map<String, Any> = cmfyClnt.queuePrompt(cmfyHostChckDao.getCmfyAuthHead(), rqstParam)
//    log.debug("[CmfyUiQueRsps] " + Gson().toJson(cmfyRsps))
//
//    // 3. Websocket => nedd to open to listen progress
//    cnntCmfyAndSendMsg(que.nodeIdx, rqstParam.clientId)
//    val ret = GnrtTextToImgRsps(
//      clientId = rqstParam.clientId,
//      stat = WbskCnst.ON_QUE_ADD
//    )
  }
}