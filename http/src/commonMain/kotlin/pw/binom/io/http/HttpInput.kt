package pw.binom.io.http

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.asyncOutput
import pw.binom.charset.Charsets
import pw.binom.copyTo
import pw.binom.io.*
import pw.binom.url.Path
import pw.binom.url.Query

interface HttpInput {
  companion object

  val path: Path
  val query: Query?

  val inputHeaders: Headers
  suspend fun readBinary(): AsyncInput
  suspend fun <T> readBinary(func: suspend (AsyncInput) -> T): T = readBinary().use { func(it) }
  suspend fun readBinary(output: AsyncOutput, bufferSize: Int = DEFAULT_BUFFER_SIZE) = readBinary { input ->
    input.copyTo(dest = output, bufferSize = bufferSize)
  }

  suspend fun readBinaryToByteArray(bufferSize: Int = DEFAULT_BUFFER_SIZE) = ByteArrayOutput().use { output ->
    readBinary(output.asyncOutput(), bufferSize = bufferSize)
    output.toByteArray()
  }

  suspend fun readText(): AsyncReader = readBinary().bufferedReader(
    charset = inputHeaders.charset?.let { Charsets.get(it) } ?: Charsets.UTF8,
  )

  suspend fun <T> readText(func: suspend (AsyncReader) -> T): T = readText().use { func(it) }
  suspend fun readAllText() = readText().use { it.readText() }
}
