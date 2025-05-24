package pw.binom.xml

class CharBuffer(limit: Int, private val onPush: (Char) -> Unit = {}) {
  class CharBufferFullException : IllegalStateException() {
    override val message: String
      get() = "CharBuffer is full"
  }

  class CharBufferEmptyException : IllegalStateException() {
    override val message: String
      get() = "CharBuffer is empty"
  }

  private val counter = CircularCounter(limit)
  private val buffer = CharArray(limit)

  val isEmpty: Boolean
    get() = counter.isEmpty

  fun get(): Char {
    if (counter.isEmpty) {
      throw CharBufferEmptyException()
    }
    return buffer[counter.pop()]
  }

  fun push(char: Char) {
    buffer[counter.push()] = char
    onPush(char)
  }

  /*
  private val buffer = CharArray(limit)
  var position = 0
  val isEmpty: Boolean
      get() = position == 0
  val isNotEmpty: Boolean
      get() = !isEmpty
  val isFull
      get() = position == buffer.size

  fun push(char: Char) {
      if (isFull) {
          throw CharBufferFullException()
      }
      buffer[position++] = char
      onPush()
  }

  fun get(): Char {
      if (isEmpty) {
          throw CharBufferEmptyException()
      }
      return buffer[--position]
  }
  */
}
