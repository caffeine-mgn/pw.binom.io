package pw.binom.security

import kotlinx.cinterop.CPointer
import platform.openssl.*
import pw.binom.io.ByteBuffer
import pw.binom.ssl.Key

const val RSA = "RSA"
const val ECDSA = "ECDSA"
const val SHA1 = "SHA1"
const val SHA256 = "SHA256"
const val SHA224 = "SHA224"
const val SHA384 = "SHA384"
const val SHA512 = "SHA512"
const val MD4 = "MD4"
const val MD5 = "MD5"
const val SM3 = "SM3"

actual interface Signature {
    actual companion object {
        actual fun getInstance(algorithm: String): Signature = when (algorithm) {
            "${SHA1}with$ECDSA" -> SignatureEcdsa(EVP_sha1()!!)
            "${SHA256}with$ECDSA" -> SignatureEcdsa(EVP_sha256()!!)
            "${SHA224}with$ECDSA" -> SignatureEcdsa(EVP_sha224()!!)
            "${SHA384}with$ECDSA" -> SignatureEcdsa(EVP_sha384()!!)
            "${SHA512}with$ECDSA" -> SignatureEcdsa(EVP_sha512()!!)
            "${SM3}with$ECDSA" -> SignatureEcdsa(EVP_sm3()!!)

            "${SHA1}with$RSA" -> SignatureRsa(EVP_sha1()!!)
            "${SHA256}with$RSA" -> SignatureRsa(EVP_sha256()!!)
            "${SHA384}with$RSA" -> SignatureRsa(EVP_sha384()!!)
            "${SHA512}with$RSA" -> SignatureRsa(EVP_sha512()!!)

            else -> throw IllegalArgumentException("Algorithm \"$algorithm\" not supported")
        }
    }

    actual fun init(key: Key.Private)
    actual fun update(data: ByteArray)
    actual fun update(data: ByteBuffer)
    actual fun sign(): ByteArray
    actual fun init(key: Key.Public)
    actual fun verify(signature: ByteArray): Boolean
}

fun CPointer<EVP_PKEY>.setKey(key: CPointer<RSA>) = EVP_PKEY_set1_RSA(this, key) == 1
