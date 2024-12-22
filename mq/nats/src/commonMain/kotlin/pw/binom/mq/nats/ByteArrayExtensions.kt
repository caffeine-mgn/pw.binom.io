package pw.binom.mq.nats

fun ByteArray.find(byte: ByteArray, startIndex: Int = 0, endIndex: Int = size): Int {
  if (size < byte.size) {
    return -1
  }
  MAIN_LOOP@ for (i in startIndex until endIndex) {
    SEARCH_LOOP@ for (j in 0 until byte.size) {
      if (this[i + j] != byte[j]) {
        break@SEARCH_LOOP
      }
      return i
    }
  }
  return -1
}

fun ByteArray.find(element: Byte, startIndex: Int = 0, endIndex: Int = size): Int {
  for (index in startIndex until endIndex) {
    if (element == this[index]) {
      return index
    }
  }
  return -1
}
