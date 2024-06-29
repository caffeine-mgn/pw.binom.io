package pw.binom.io

internal class AsyncInputWithLimit(val limit: Long, val source: AsyncInput) : AsyncInput {

  var remaining: Long = limit
    private set

  override val available: Int
    get() = if (remaining <= 0) 0 else minOf(source.available, remaining.toInt())

  override suspend fun read(dest: ByteBuffer): Int {
    val rem = dest.remaining
    if (remaining <= 0) {
      return 0
    }
    if (rem <= 0) {
      return 0
    }
    if (rem > remaining) {
      dest.limit = dest.position + remaining.toInt()
    }
    val wasRead = source.read(dest)
    if (wasRead > 0) {
      remaining -= wasRead
    }
    return wasRead
  }

  override suspend fun asyncClose() {
    remaining = 0
    source.asyncClose()
  }
}
