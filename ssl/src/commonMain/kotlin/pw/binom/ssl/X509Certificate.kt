package pw.binom.ssl

import pw.binom.io.Closeable

expect class X509Certificate : Closeable {
    companion object {
        fun load(data: ByteArray): X509Certificate
    }

    fun save(): ByteArray
}
