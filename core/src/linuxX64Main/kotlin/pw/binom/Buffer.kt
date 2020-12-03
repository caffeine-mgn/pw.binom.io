package pw.binom

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CValuesRef

actual interface Buffer {
    fun refTo(position: Int): CValuesRef<ByteVar>
    actual val remaining: Int
    actual var position: Int
    actual var limit: Int
    actual val capacity: Int
    actual fun flip()
    actual fun compact()
    actual fun clear()
}