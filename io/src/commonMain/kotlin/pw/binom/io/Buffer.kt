package pw.binom.io

/**
 * Calls [Buffer.clear] and returns [this]
 */
fun <T : Buffer> T.clean(): T {
  clear()
  return this
}

expect interface Buffer : CommonBuffer {
  companion object;
}
