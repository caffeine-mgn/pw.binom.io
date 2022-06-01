package pw.binom.security

import kotlinx.cinterop.CPointer
import platform.openssl.*
import pw.binom.getSslError
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm
import pw.binom.ssl.createEcdsaFromPrivateKey
import pw.binom.ssl.createEcdsaFromPublicKey

class SignatureEcdsa(messageDigest: CPointer<EVP_MD>) : AbstractSignature(messageDigest) {
    override fun isAlgorithmSupport(algorithm: KeyAlgorithm) = algorithm == KeyAlgorithm.ECDSA

    override fun createPkey(key: Key.Private): CPointer<EVP_PKEY> {
        val ecKey = createEcdsaFromPrivateKey(key.data)
        val pkey = EVP_PKEY_new()!!
        if (!pkey.setKey(ecKey)) {
            EVP_PKEY_free(pkey)
            throw SignatureException("EVP_PKEY_set1_EC_KEY failed")
        }
        return pkey
    }

    override fun createPkey(key: Key.Public): CPointer<EVP_PKEY> {
        println("-->1# getSslError()->${getSslError()}")
        val ecKey = createEcdsaFromPublicKey(key.data)
        println("-->2# getSslError()->${getSslError()}")
        val pkey = EVP_PKEY_new()!!
        println("-->3# getSslError()->${getSslError()}")
        if (!pkey.setKey(ecKey)) {
            EVP_PKEY_free(pkey)
            throw SignatureException("EVP_PKEY_set1_EC_KEY failed")
        }
        return pkey
    }
}

private fun CPointer<EVP_PKEY>.setKey(key: CPointer<EC_KEY>) = EVP_PKEY_set1_EC_KEY(this, key) == 1
