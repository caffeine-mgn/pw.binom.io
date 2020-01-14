package pw.binom

import pw.binom.io.Closeable

expect class FloatDataBuffer : Closeable {
    companion object {
        fun alloc(size: Int): FloatDataBuffer
    }

    val size: Int
    operator fun set(index: Int, value: Float)
    operator fun get(index: Int): Float
}