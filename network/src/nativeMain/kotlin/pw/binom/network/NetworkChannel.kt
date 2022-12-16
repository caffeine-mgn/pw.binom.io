package pw.binom.network

import pw.binom.io.Closeable

actual interface NetworkChannel : Closeable {
    fun setKey(key: AbstractNativeKey)
    fun keyClosed()
    val native: RawSocket?
    val nNative: NSocket?
}
