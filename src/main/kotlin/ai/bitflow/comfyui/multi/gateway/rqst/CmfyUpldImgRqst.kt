package ai.bitflow.comfyui.multi.gateway.rqst

import jakarta.ws.rs.core.MediaType
import org.jboss.resteasy.reactive.PartType
import org.jboss.resteasy.reactive.RestForm
import java.io.File

/**
 * Handling Multipart Form data
 * https://quarkus.io/guides/rest#multipart
 */
class CmfyUpldImgRqst {

  constructor(file: File, fileName: String): super() {
    this.image = CmfyUpldImg(file, fileName)
  }

//  @RestForm
  var image: CmfyUpldImg

  @RestForm
  var type: String = "image/webp"

  @RestForm
  var overwrite: Boolean = true

}

class CmfyUpldImg (

  @RestForm
  @PartType(MediaType.APPLICATION_OCTET_STREAM)
  var file: File,  // InputStream

  @RestForm  // @PartType(MediaType.TEXT_PLAIN)
  var fileName: String,

)