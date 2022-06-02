package pw.binom.crypto

import pw.binom.ssl.Key

expect class ECPublicKey : Key.Public {
    val q: EcPoint

    companion object {
        fun load(data: ByteArray): ECPublicKey
    }
}
