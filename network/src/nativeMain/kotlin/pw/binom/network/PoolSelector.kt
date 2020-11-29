package pw.binom.network

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.COpaquePointer
import pw.binom.concurrency.Reference
import pw.binom.io.Closeable

expect class PoolSelector() : Closeable {
    fun attach(socket: NSocket, mode: Int, attachment: Any?): NativeSelectorKey
    fun wait(timeout: Long = -1, func: (attachment: NativeSelectorKey, mode: Int) -> Unit): Boolean

    class NativeSelectorKey : Closeable {
        var status: Int
        var mode: Int
        val attachment: Reference<Any>?
        var socket: NSocket
    }
}
