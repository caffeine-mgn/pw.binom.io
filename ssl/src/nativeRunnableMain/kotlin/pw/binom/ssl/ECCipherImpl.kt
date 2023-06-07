package pw.binom.ssl

import kotlinx.cinterop.reinterpret
import pw.binom.crypto.ECPrivateKey
import pw.binom.crypto.ECPublicKey

class ECCipherImpl(override val algoritm: String) : OpenSSLCipher() {
    private var key: ECKey? = null
    override fun init(mode: Cipher.Mode, key: Key) {
        val keyPtr = when (key) {
            is ECPrivateKey -> key.native
            is ECPublicKey -> key.native
            else -> throw IllegalArgumentException("Key ${key::class.simpleName} not supported")
        }
        this.key = key as ECKey
        init(mode = mode, key = keyPtr.reinterpret(), params = emptyMap())
    }
}
