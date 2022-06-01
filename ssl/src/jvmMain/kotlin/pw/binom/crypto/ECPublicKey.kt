package pw.binom.crypto

import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm

actual class ECPublicKey(val native: BCECPublicKey) : Key.Public {
    override val algorithm: KeyAlgorithm
        get() = KeyAlgorithm.ECDSA
    override val data: ByteArray
        get() = native.encoded
    actual val q: EcPoint by lazy {
        EcPoint(native.q, ECCurve(native.q.curve))
    }
}
