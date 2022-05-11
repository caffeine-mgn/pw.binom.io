package pw.binom.ssl

import pw.binom.io.Closeable

expect interface PublicKey : Closeable {
    companion object

    val algorithm: KeyAlgorithm
    val data: ByteArray
}

expect fun PublicKey.Companion.loadRSA(data: ByteArray): PublicKey
