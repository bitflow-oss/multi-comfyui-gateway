package ai.bitflow.comfyui.multi.gateway.dao

import ai.bitflow.comfyui.multi.gateway.excn.CmfyQueEctn
import ai.bitflow.comfyui.multi.gateway.rsps.CmfyGetQueRsps
import com.google.gson.Gson
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.RestClientBuilder
import org.jboss.logging.Logger
import java.net.URI

@ApplicationScoped
class CmfyHostChckDao {

  @Inject
  lateinit var log: Logger

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

  private var comfyHosts = mutableListOf<String>()

  fun getComfyHostSize(): Int {
    return comfyHosts.size
  }

  fun getCmfyAuthHead(): String {
    return "Bearer $CMFY_API_KEY"
  }

  fun getComfyHost(): MutableList<String> {
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
    val hostName = getComfyHost()
    if (idx < 0 || idx >= hostName.size) {
      throw CmfyQueEctn()
    } else {
      return getComfyHost()[idx]!!
    }
  }

}