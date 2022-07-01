package pw.binom.io

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.convert
import platform.posix.memcpy

actual fun CPointer<ByteVar>.copy(dest: CPointer<ByteVar>, size: Long) {
    memcpy(dest, this, size.convert())
}
