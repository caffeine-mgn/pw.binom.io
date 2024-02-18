package pw.binom.ssl

import pw.binom.io.Closeable

expect interface PrivateKey : Closeable {
  companion object

  val algorithm: KeyAlgorithm
  val data: ByteArray
}

expect fun PrivateKey.Companion.loadRSAFromPem(data: ByteArray): PrivateKey
