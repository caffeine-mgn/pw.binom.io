package pw.binom.io.file

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.charset.Charset
import pw.binom.charset.Charsets
import pw.binom.copyTo
import pw.binom.io.*

fun File.append(text: String, charset: Charset = Charsets.UTF8) {
  openWrite(true).bufferedWriter(charset = charset).use {
    it.append(text)
  }
}

fun File.append(data: ByteBuffer) {
  openWrite(append = true).use {
    it.write(data)
  }
}
fun File.append(data: ByteArray) {
  data.wrap { buf ->
    append(buf)
  }
}

/**
 * Rewrite [text] to current file. If file not exists will create it
 */
fun File.rewrite(
  text: String,
  charset: Charset = Charsets.UTF8,
  bufferSize: Int = DEFAULT_BUFFER_SIZE,
  charBufferSize: Int = bufferSize / 2,
) {
  openWrite(false).bufferedWriter(
    charset = charset,
    bufferSize = bufferSize,
    charBufferSize = charBufferSize,
  ).use {
    it.append(text)
  }
}

/**
 * Returns all file content. Reading using [charset]
 */
fun File.readText(
  charset: Charset = Charsets.UTF8,
  bufferSize: Int = DEFAULT_BUFFER_SIZE,
  charBufferSize: Int = bufferSize,
) = openRead()
  .bufferedReader(
    charset = charset,
    bufferSize = bufferSize,
    charBufferSize = charBufferSize,
  )
  .use { it.readText() }

fun File.readBinary(
  bufferSize: Int = DEFAULT_BUFFER_SIZE,
): ByteArray {
  ByteArrayOutput().use { out ->
    openRead().use { it.copyTo(out, bufferSize = bufferSize) }
    return out.toByteArray()
  }
}

fun File.writeBinary(data: ByteBuffer) {
  openWrite().use {
    it.write(data)
  }
}

fun File.writeBinary(data: ByteArray) {
  data.wrap { buf ->
    writeBinary(buf)
  }
}

fun File.rewrite(
  data: ByteArray,
) {
  openWrite(append = false).use {
    data.wrap { buf ->
      it.write(buf)
    }
  }
}
