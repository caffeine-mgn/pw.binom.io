package pw.binom

import pw.binom.io.Closeable

expect class ByteDataBuffer : Closeable {
    companion object {
        fun alloc(size: Int): ByteDataBuffer
    }
    val size:Int
    operator fun set(index: Int, value: Byte)
    operator fun get(index: Int): Byte
}