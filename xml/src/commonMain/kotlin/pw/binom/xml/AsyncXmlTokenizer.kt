package pw.binom.xml

class AsyncXmlTokenizer(override val reader: AsyncBufferedReader):AbstractXmlTokenizer() {

  companion object;

  private suspend fun nextChar(): Char =
    reader.readChar() ?: throw EOFException()

  private suspend fun readChar(): Char = nextChar()

  suspend fun next() = next { readChar() }
}
