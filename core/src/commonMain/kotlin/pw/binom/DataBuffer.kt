package pw.binom

import pw.binom.io.Closeable

expect class DataBuffer : Closeable {
    companion object {
        fun alloc(size: Long): DataBuffer
    }
}