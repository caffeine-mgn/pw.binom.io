package pw.binom.network

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.COpaquePointer
import pw.binom.io.Closeable

expect class PoolSelector(): Closeable {
    fun attach(socket: NSocket, mode: Int, attachment: COpaquePointer?)
    fun detach(socket: NSocket)
    fun edit(socket: NSocket, mode: Int,attachment: COpaquePointer?)
    fun wait(timeout: Long = -1, func: (attachment: COpaquePointer?, mode: Int) -> Unit):Boolean
}