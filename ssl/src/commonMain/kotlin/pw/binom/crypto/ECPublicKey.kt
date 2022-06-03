package pw.binom.crypto

import pw.binom.ssl.ECKey
import pw.binom.ssl.Key

expect class ECPublicKey : Key.Public, ECKey {
    val q: EcPoint

    companion object {
        fun load(data: ByteArray): ECPublicKey
    }
}
