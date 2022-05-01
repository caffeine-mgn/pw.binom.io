package pw.binom.ssl

import pw.binom.io.Closeable
import java.security.PublicKey

actual interface PublicKey : Closeable {
    actual companion object;

    actual val algorithm: KeyAlgorithm
    val native: PublicKey
    actual val data: ByteArray
}
