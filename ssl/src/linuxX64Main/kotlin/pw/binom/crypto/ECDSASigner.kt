package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.openssl.*
import pw.binom.BigNum
import pw.binom.checkTrue
import pw.binom.throwError
import pw.binom.toBigNum

actual class ECDSASigner {
    private val privateKey: ECPrivateKey?
    private val publicKey: ECPublicKey?

    actual constructor(privateKey: ECPrivateKey) {
        this.privateKey = privateKey
        this.publicKey = null
    }

    actual constructor(publicKey: ECPublicKey) {
        this.privateKey = null
        this.publicKey = publicKey
    }

    actual fun sign(data: ByteArray): ECDSASignature {
        check(privateKey != null) { "ECDSASigner inited for verify" }
        val signature = data.usePinned { pinned ->
            ECDSA_do_sign(
                pinned.addressOf(0).reinterpret(),
                pinned.get().size,
                privateKey.native,
            ) ?: throwError("Can't sign")
        }
        val r = ECDSA_SIG_get0_r(signature) ?: throwError("Can't get R component")
        val s = ECDSA_SIG_get0_s(signature) ?: throwError("Can't get S component")
        val rr = BigNum(r).toBigInt()
        val ss = BigNum(s).toBigInt()
        ECDSA_SIG_free(signature)
        return ECDSASignature(
            r = rr,
            s = ss,
        )
    }

    actual fun verify(
        data: ByteArray,
        r: BigInteger,
        s: BigInteger
    ): Boolean {
        val publicKey = publicKey ?: throw IllegalStateException("ECDSASigner inited for verify")
        val sign = ECDSA_SIG_new() ?: throwError("ECDSA_SIG_new fails")
        return r.toBigNum().use { rBn ->
            s.toBigNum().use { sBn ->
                ECDSA_SIG_set0(sign, rBn.ptr, sBn.ptr).checkTrue("ECDSA_SIG_set0 fails") {
                    ECDSA_SIG_free(sign)
                }

                val result = data.usePinned { pinned ->
                    ECDSA_do_verify(
                        pinned.addressOf(0).reinterpret(),
                        pinned.get().size.convert(),
                        sign,
                        publicKey.native,
                    ) > 0
                }
                ECDSA_SIG_free(sign)
                result
            }
        }
    }

    actual fun verify(data: ByteArray, signature: ECDSASignature) = verify(data, signature.r, signature.s)
}
