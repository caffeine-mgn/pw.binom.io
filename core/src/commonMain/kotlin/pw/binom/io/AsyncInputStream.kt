package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.asUTF8String
import pw.binom.fromBytes
import pw.binom.internal_readln
import pw.binom.pool.ObjectPool
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
internal val numberArray = ByteArray(Long.SIZE_BYTES)

@Deprecated("Use AsyncInput")
interface AsyncInputStream : AsyncCloseable {
    /**
     * Reads one byte from stream
     * @return byte from stream
     * @throws EOFException when stream is empty
     */
    suspend fun read(): Byte

    suspend fun skip(length: Long): Long = 0L

    suspend fun read(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): Int
}