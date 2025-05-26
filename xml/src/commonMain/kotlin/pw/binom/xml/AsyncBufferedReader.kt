package pw.binom.xml

import pw.binom.io.AsyncReader

class AsyncBufferedReader(val reader: AsyncReader, bufferSize: Int) : AbstractBufferedReader(bufferSize), AsyncReader {
  override suspend fun readChar(): Char? = readChar { reader.readChar() }
  override suspend fun asyncClose() {
    reader.asyncClose()
  }
}
