package pw.binom.ssl

import pw.binom.io.Closeable
import java.security.PrivateKey

actual interface PrivateKey : Closeable {
    actual val algorithm: KeyAlgorithm
    val native: PrivateKey
    actual val data: ByteArray

    actual companion object
}
