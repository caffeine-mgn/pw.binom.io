package pw.binom.io.http

import pw.binom.charset.Charsets
import pw.binom.io.*

interface HttpOutput {
  companion object

  val headers: MutableHeaders
  suspend fun send(text: String) = writeText().use { it.append(text);it.getInput() }

  suspend fun send(bytes: ByteArray) = bytes.wrap { send(it) }
  suspend fun send(bytes: ByteBuffer) = writeBinary().use { it.writeFully(bytes);it.getInput() }
  suspend fun writeText(): HttpAsyncWriter {
    val output = writeBinary()
    val writer = output.bufferedWriter(
      charset = headers.contentEncoding?.let { Charsets.get(it) } ?: Charsets.UTF8,
    )
    return HttpAsyncWriterAdapter(
      writer = writer,
      output = output
    )
  }

  suspend fun writeBinary(): HttpAsyncOutput
  suspend fun writeBinary(func: suspend (HttpAsyncOutput) -> Unit) = writeBinary().use { func(it);it.getInput() }
  suspend fun writeText(func: suspend (AsyncWriter) -> Unit) = writeText().use { func(it);it.getInput() }
}
