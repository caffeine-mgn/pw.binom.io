package pw.binom.xml

import pw.binom.io.Reader

class SyncBufferedReader(val reader: Reader, bufferSize: Int) : Reader, AbstractBufferedReader(bufferSize) {
  override fun read() = readChar { reader.read() }
  override fun close() {
    reader.close()
  }
}
