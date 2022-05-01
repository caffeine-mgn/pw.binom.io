package pw.binom.ssl

import kotlinx.cinterop.CPointer
import platform.openssl.EVP_PKEY
import platform.openssl.EVP_PKEY_get1_RSA
import platform.openssl.i2d_RSAPrivateKey_bio
import platform.openssl.i2d_RSAPublicKey_bio
import pw.binom.io.ByteArrayOutput

class PublicKeyImpl(override val algorithm: KeyAlgorithm, override val native: CPointer<EVP_PKEY>):PublicKey {
    override val data: ByteArray
        get() {
            when (algorithm) {
                KeyAlgorithm.RSA -> {
                    val rsa = EVP_PKEY_get1_RSA(native) ?: TODO("EVP_PKEY_get1_RSA returns null")
                    val b = Bio.mem()
                    i2d_RSAPublicKey_bio(b.self, rsa)
                    val o = ByteArrayOutput()
                    b.copyTo(o)
                    b.close()
                    o.data.flip()
                    val array = o.data.toByteArray()
                    o.close()
                    return array
                }
            }
        }

    override fun close() {
        TODO("Not yet implemented")
    }
}