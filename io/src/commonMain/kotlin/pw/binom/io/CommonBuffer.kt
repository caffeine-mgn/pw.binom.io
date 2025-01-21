package pw.binom.io

interface CommonBuffer {
  companion object;
  var position: Int
  var limit: Int
  val capacity: Int
  val elementSizeInBytes: Int
  val remaining: Int
    get() = limit - position
  val hasRemaining: Boolean
    get() = remaining > 0

  fun flip() {
    val oldPosition = position
    position = 0
    limit = oldPosition
  }

  fun compact()
  fun clear() {
    limit = capacity
    position = 0
  }
}
