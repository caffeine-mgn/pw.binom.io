package pw.binom.ssl

import kotlinx.atomicfu.atomic
import kotlinx.cinterop.*
import platform.openssl.*
import pw.binom.io.ByteArrayOutput
import pw.binom.io.IOException
import pw.binom.io.use

private val loaded = atomic(false)
internal fun loadOpenSSL() {
    if (loaded.compareAndSet(false, true)) {
        OPENSSL_init_crypto(
            (OPENSSL_INIT_ADD_ALL_CIPHERS or OPENSSL_INIT_ADD_ALL_DIGESTS or OPENSSL_INIT_LOAD_CONFIG).convert(),
            null
        )
    }
}

fun createRsaFromPublicKey(data: ByteArray) = Bio.mem(data).use { priv ->
    val vvv = PEM_read_bio_X509(priv.self, null, null, null)
    PEM_read_bio_RSAPublicKey(priv.self, null, null, null) ?: throw IOException("Can't load public key")
}

fun createRsaFromPrivateKey(data: ByteArray) = Bio.mem(data).use { priv ->
    PEM_read_bio_RSAPrivateKey(priv.self, null, null, null) ?: throw IOException("Can't load public key")
}

var CPointer<RSA>.publicKey: ByteArray
    get() = Bio.mem().use { b ->
        PEM_write_bio_RSAPublicKey(b.self, this)
        b.toByteArray()
    }
    set(value) {
        Bio.mem(value).use { priv ->
            PEM_read_bio_RSAPublicKey(priv.self, reinterpret(), null, null)
                ?: throw IOException("Can't load public key")
        }
    }

var CPointer<RSA>.privateKey: ByteArray
    get() = Bio.mem().use { b ->
        PEM_write_bio_RSAPrivateKey(b.self, this, null, null, 0, null, null)
        b.toByteArray()
    }
    set(value) {
        Bio.mem(value).use { priv ->
            PEM_read_bio_RSAPrivateKey(priv.self, reinterpret(), null, null)
                ?: throw IOException("Can't load private key")
        }
    }

val CPointer<RSA>.dataSize
    get() = RSA_size(this)

var CPointer<EVP_PKEY>.rsa: CPointer<RSA>?
    get() = EVP_PKEY_get0_RSA(this)
    set(value) {
        EVP_PKEY_set1_RSA(this, value)
    }

object KeyUtils {

    fun CPointer<RSA>.loadPublic(data: ByteArray): CPointer<RSA> {
        Bio.mem(data).use { priv ->
            d2i_RSAPublicKey_bio(priv.self, reinterpret())
        }
        return this
    }

    fun CPointer<RSA>.loadPrivate(data: ByteArray): CPointer<RSA> {
        Bio.mem(data).use { priv ->
            d2i_RSAPrivateKey_bio(priv.self, reinterpret())
        }
        return this
    }

    fun createKeyPair(publicKey: ByteArray, privateKey: ByteArray): KeyGenerator.KeyPair {
        val rsa = RSA_new()!!
        Bio.mem(privateKey).use { priv ->
            d2i_RSAPrivateKey_bio(priv.self, rsa.reinterpret())
        }
        Bio.mem(publicKey).use { priv ->
            d2i_RSAPublicKey_bio(priv.self, rsa.reinterpret())
        }
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
