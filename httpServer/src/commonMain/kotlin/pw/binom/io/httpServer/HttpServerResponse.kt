package pw.binom.io.httpServer

import pw.binom.charset.Charsets
import pw.binom.io.AsyncOutput
import pw.binom.io.AsyncWriter
import pw.binom.io.bufferedWriter
import pw.binom.io.http.HttpOutput
import pw.binom.io.http.MutableHeaders

interface HttpServerResponse : HttpOutput {
  override val headers: MutableHeaders
  var status: Int
  val responseStarted: Boolean
  suspend fun startOutput(): AsyncOutput
  suspend fun startWriter(): AsyncWriter =
    startOutput().bufferedWriter(charset = headers.charset?.let { Charsets.get(it) } ?: Charsets.UTF8)
}
