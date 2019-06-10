package pw.binom.ssl

import kotlinx.cinterop.CPointer
import platform.openssl.EVP_PKEY
import pw.binom.io.Closeable

actual interface PrivateKey:Closeable{
    actual val algorithm: KeyAlgorithm
    val native: CPointer<EVP_PKEY>
    actual val data: ByteArray

    actual companion object
}