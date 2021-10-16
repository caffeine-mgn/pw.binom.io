package pw.binom

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef

actual interface Buffer {
    fun<T> refTo(position: Int,func:(CPointer<ByteVar>)->T):T
    actual val remaining: Int
    actual var position: Int
    actual var limit: Int
    actual val capacity: Int
    actual fun flip()
    actual fun compact()
    actual fun clear()
    actual val elementSizeInBytes: Int
}