package pw.binom.memory

actual fun ByteArray.fill(value: Byte) {
  for (i in 0..size - 1) {
    this[i] = value
  }
}
