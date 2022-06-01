package pw.binom.crypto

import platform.openssl.EC_POINT_point2bn
import platform.openssl.POINT_CONVERSION_UNCOMPRESSED
import pw.binom.BigNum
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm
import pw.binom.throwError

actual class ECPublicKey(private val curve: ECCurve, actual val q: EcPoint) : Key.Public {
    override val algorithm: KeyAlgorithm
        get() = KeyAlgorithm.ECDSA
    override val data: ByteArray
        get() = TODO("Not yet implemented")

    fun aa() {
        val publicBiginteger = BigNum().use { bn ->
            EC_POINT_point2bn(curve.native, q.ptr, POINT_CONVERSION_UNCOMPRESSED, bn.ptr, null)
                ?: throwError("EC_POINT_point2bn fails")
            bn.toBigInt()
        }
    }
}
