package pw.binom.io

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer

expect fun CPointer<ByteVar>.copy(dest: CPointer<ByteVar>, size: Long)
