package pw.binom.crypto

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.openssl.ECDSA_SIG_free
import platform.openssl.ECDSA_SIG_get0_r
import platform.openssl.ECDSA_SIG_get0_s
import platform.openssl.ECDSA_do_sign
import pw.binom.BigNum
import pw.binom.throwError

actual object ECDSASigner {
    actual fun sign(data: ByteArray, privateKey: ECPrivateKey): ECDSASignature {
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
}
