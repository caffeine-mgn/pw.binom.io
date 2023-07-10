package pw.binom.io.httpServer

import pw.binom.charset.Charsets
import pw.binom.io.*
import pw.binom.io.http.MutableHeaders

interface HttpServerResponse {
  val headers: MutableHeaders
  var status: Int
  val responseStarted: Boolean
  suspend fun startOutput(): AsyncOutput
  suspend fun startWriter(): AsyncWriter =
    startOutput().bufferedWriter(charset = headers.charset?.let { Charsets.get(it) } ?: Charsets.UTF8)

  suspend fun send(text: String) {
    startWriter().use {
      it.append(text)
    }
  }

  suspend fun send(data: ByteBuffer) {
    startOutput().use {
      it.writeFully(data)
    }
  }

  suspend fun send(data: ByteArray) {
    data.wrap {
      send(it)
    }
  }
}
