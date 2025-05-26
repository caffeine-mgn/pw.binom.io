package pw.binom.xml

import pw.binom.io.Reader

class SyncXmlTokenizer(override val reader: SyncBufferedReader) : AbstractXmlTokenizer() {
  constructor(reader: Reader) : this(SyncBufferedReader(reader, 10))

  companion object;


  private fun nextChar(): Char =
    reader.read() ?: throw EOFException()

  private fun readChar(): Char = nextChar()

  fun next() = next { readChar() }
}
