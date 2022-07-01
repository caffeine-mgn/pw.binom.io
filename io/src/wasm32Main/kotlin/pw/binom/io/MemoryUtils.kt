package pw.binom.io

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.get
import kotlinx.cinterop.set

actual fun CPointer<ByteVar>.copy(dest: CPointer<ByteVar>, size: Long) {
    var c = 0L
    while (c < size) {
        dest[c] = get(c)
        c++
    }
}
