package pw.binom.network

import pw.binom.io.Closeable

actual interface NetworkChannel : Closeable {
    fun addKey(key: AbstractKey)
    fun removeKey(key: AbstractKey)
}
