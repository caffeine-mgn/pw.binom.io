package pw.binom.io.http

import pw.binom.io.AsyncWriter

internal class HttpAsyncWriterAdapter(val writer: AsyncWriter, val output: HttpAsyncOutput) : HttpAsyncWriter {
  override suspend fun getInput(): HttpInput? = output.getInput()

  override suspend fun append(value: CharSequence?): HttpAsyncWriter {
    writer.append(value)
    return this
  }

  override suspend fun append(value: Char): HttpAsyncWriter {
    writer.append(value)
    return this
  }

  override suspend fun append(value: CharSequence?, startIndex: Int, endIndex: Int): HttpAsyncWriter {
    writer.append(value = value, startIndex = startIndex, endIndex = endIndex)
    return this
  }

  override suspend fun flush() {
    writer.flush()
  }

  override suspend fun asyncClose() {
    writer.asyncClose()
  }
}
