package ai.bitflow.comfyui.multi.gateway.dao

import io.quarkus.qute.*
import io.quarkus.qute.TemplateLocator.TemplateLocation
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import org.jboss.logging.Logger
import java.io.FileReader
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.util.*

/**
 * Using Qute with templates from a database
 * https://quarkus.io/blog/qute-templates-from-db/
 *
 * val template = jsonStrToTplt(jsonStr)
 * val jsonMap = Gson().toJson(jsonStr, LinkedTreeMap::class.java)
 */
//@Locate("*.json")
@ApplicationScoped
class HostJsonTpltLctr : TemplateLocator {

  @Inject
  private lateinit var log: Logger

  @Inject
  private lateinit var tpltEngn: Engine


  override fun locate(filePath: String): Optional<TemplateLocation> {
    val jsonStr = loadHostJsonString(filePath)
    log.debug("Template found in the host path $filePath")
    return Optional.of<TemplateLocation?>(buildTemplateLocation(jsonStr))
  }

  override fun getPriority(): Int {
    return WithPriority.DEFAULT_PRIORITY - 1
  }

  @Throws(IOException::class)
  private fun loadHostJsonString(jsonFilePath: String): String {
    val reader: Reader = FileReader(jsonFilePath)
    return reader.readText()
  }

  private fun jsonStrToTplt(jsonStr: String): Template {
    return tpltEngn.parse(jsonStr)
  }

  fun configureEngine(@Observes builder: EngineBuilder) {
    builder.addLocator(this)
  }

  private fun buildTemplateLocation(templateContent: String): TemplateLocation {

    return object : TemplateLocation {

      override fun read(): Reader {
        return StringReader(templateContent)
      }

      override fun getVariant(): Optional<Variant> {
        return Optional.empty<Variant>()
      }
    }

  }

}