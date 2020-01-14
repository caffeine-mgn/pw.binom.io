package pw.binom

import pw.binom.io.Closeable

expect class IntDataBuffer : Closeable {
    companion object {
        fun alloc(size: Int): IntDataBuffer
    }

    val size:Int
    operator fun set(index: Int, value: Int)
    operator fun get(index: Int): Int
}