package pw.binom.io.httpClient

import pw.binom.io.*
import kotlin.use

interface HttpRequestBody : AsyncCloseable {
  val isFlushed: Boolean
  val isOutputStarted: Boolean
  val input: AsyncInput
  val output: AsyncOutput

  suspend fun startWriteBinary(): AsyncOutput

  suspend fun startWriteText(): AsyncWriter {
    check(!isFlushed) { "Response already flushed" }
    return startWriteBinary().bufferedWriter(closeParent = false)
  }

  suspend fun flush(): HttpResponse

  suspend fun send(text: String): HttpResponse {
    startWriteText().use {
      it.append(text)
    }
    return flush()
  }

  suspend fun send(data: ByteBuffer): HttpResponse {
    startWriteBinary().use {
      it.write(data)
    }
    return flush()
  }
}
