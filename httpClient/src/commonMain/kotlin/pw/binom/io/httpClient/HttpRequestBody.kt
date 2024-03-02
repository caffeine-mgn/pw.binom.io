package pw.binom.io.httpClient

import pw.binom.io.*

interface HttpRequestBody : AsyncCloseable {
  val isFlushed: Boolean
  val isOutputStarted: Boolean
  val input: AsyncInput
  val output: AsyncOutput
  val mainChannel: AsyncChannel

  suspend fun startWriteBinary(): AsyncOutput

  suspend fun startWriteText(): AsyncWriter {
    check(!isFlushed) { "Response already flushed" }
    return startWriteBinary().bufferedWriter(closeParent = false)
  }

  suspend fun flush(): HttpResponse

  suspend fun send(text: String): HttpResponse {
    startWriteText().useAsync {
      it.append(text)
    }
    return flush()
  }

  suspend fun send(data: ByteBuffer): HttpResponse {
    startWriteBinary().useAsync {
      it.write(data)
    }
    return flush()
  }
}
