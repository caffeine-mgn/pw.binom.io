package pw.binom.io

import kotlin.random.Random

fun Random.nextBytes(dest: ByteBuffer) {
  dest.holdState {
    while (it.remaining > 0) {
      it.put(nextInt().toByte())
    }
  }
}
