package pw.binom.ssl

import org.bouncycastle.crypto.encodings.PKCS1Encoding
import org.bouncycastle.crypto.engines.RSAEngine
import java.security.KeyPairGenerator

actual fun Key.Companion.generateRsa(size: Int): Key.Pair {
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
    keyPairGenerator.initialize(size)
    val pair = keyPairGenerator.generateKeyPair()
    println("")
    val bbbb = pair.public
    val bb = bbbb.encoded
    val encode = PKCS1Encoding(RSAEngine())
//    encode.init(true,)
    return Key.Pair(
        public = Key.Public(algorithm = KeyAlgorithm.RSA, data = pair.public.encoded),
        private = Key.Private(algorithm = KeyAlgorithm.RSA, data = pair.private.encoded),
    )
}
