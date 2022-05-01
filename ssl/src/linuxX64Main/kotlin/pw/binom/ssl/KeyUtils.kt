package pw.binom.ssl

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.reinterpret
import platform.openssl.*
import pw.binom.io.ByteArrayOutput
import pw.binom.io.use

object KeyUtils {

    fun createKeyPair(publicKey: ByteArray, privateKey: ByteArray): KeyGenerator.KeyPair {
        val rsa = RSA_new()!!
        Bio.mem(privateKey).use { priv ->
            d2i_RSAPrivateKey_bio(priv.self, rsa.reinterpret())
        }
        Bio.mem(publicKey).use { priv ->
            d2i_RSAPublicKey_bio(priv.self, rsa.reinterpret())
        }
        val k = EVP_PKEY_new()!!
        EVP_PKEY_set1_RSA(k, rsa)
        val pair = EVP_PKEY_new()!!
        EVP_PKEY_set1_RSA(pair, rsa)
        return KeyGenerator.KeyPair(pair)
    }

    fun getPublicKey(native: CPointer<EVP_PKEY>, algorithm: KeyAlgorithm): ByteArray {
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

    fun getPrivateKey(native: CPointer<EVP_PKEY>, algorithm: KeyAlgorithm): ByteArray {
        when (algorithm) {
            KeyAlgorithm.RSA -> {
                val rsa = EVP_PKEY_get1_RSA(native) ?: TODO("EVP_PKEY_get1_RSA returns null")
                val b = Bio.mem()
                i2d_RSAPrivateKey_bio(b.self, rsa)
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
}
