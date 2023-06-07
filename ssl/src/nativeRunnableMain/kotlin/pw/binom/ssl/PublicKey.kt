package pw.binom.ssl

import kotlinx.cinterop.CPointer
import platform.openssl.EVP_PKEY
import pw.binom.io.Closeable

actual interface PublicKey : Closeable {
    actual companion object

    val native: CPointer<EVP_PKEY>
    actual val algorithm: KeyAlgorithm
    actual val data: ByteArray
}
