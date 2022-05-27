@file:JvmName("ByteBufferCommonKt")

package pw.binom.io

import pw.binom.pool.ObjectPool
import pw.binom.pool.PoolObjectFactory
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.jvm.JvmName

/**
 * A part of memory. Also contents current read/write state
 */
expect class ByteBuffer : Channel, Buffer, ByteBufferProvider {
    companion object {
        fun alloc(size: Int): ByteBuffer
        fun alloc(size: Int, onClose: (ByteBuffer) -> Unit): ByteBuffer
    }

    fun realloc(newSize: Int): ByteBuffer
    fun skip(length: Long): Long
    fun put(value: Byte)
    fun getByte(): Byte
    operator fun get(index: Int): Byte
    fun read(dest: ByteArray, offset: Int = 0, length: Int = dest.size - offset): Int

    /**
     * Returns last byte. Work as [getByte] but don'tm move position when he reads
     */
    fun peek(): Byte
    fun reset(position: Int, length: Int): ByteBuffer
    fun write(
        data: ByteArray,
        offset: Int = 0, /*= 0*/
        length: Int = calcLength(
            self = this,
            data = data,
            offset = offset
        )/* = minOf(data.size - offset, remaining)*/
    ): Int

    operator fun set(index: Int, value: Byte)

    /**
     * push all available data (between [position] and [limit]) from this bytebuffer to bytearray.
     * Don't change [position] and [limit] of current buffer. Thread unsafe.
     */
    fun toByteArray(limit: Int): ByteArray
    fun toByteArray(): ByteArray
    fun subBuffer(index: Int, length: Int): ByteBuffer
    fun free()
}

internal fun calcLength(self: ByteBuffer, data: ByteArray, offset: Int) = minOf(data.size - offset, self.remaining)

fun ByteBuffer.empty(): ByteBuffer {
    position = 0
    limit = 0
    return this
}

class ByteBufferFactory(val size: Int) : PoolObjectFactory<ByteBuffer> {
    override fun new(pool: ObjectPool<ByteBuffer>): ByteBuffer = ByteBuffer.alloc(size) { pool.recycle(it) }

    override fun new(): ByteBuffer = ByteBuffer.alloc(size)

    override fun free(value: ByteBuffer) {
        value.close()
    }
}

interface ByteBufferAllocator : ObjectPool<ByteBuffer>, ByteBufferProvider {
    override fun reestablish(buffer: ByteBuffer) {
        recycle(buffer)
    }

    override fun get(): ByteBuffer = borrow()
}

/**
 * Provide access to buffer
 */
interface ByteBufferProvider {
    /**
     * Returns buffer
     */
    fun get(): ByteBuffer

    /**
     * Return object to provider. [buffer] should be not closed. [buffer] should be same returned by [get]
     */
    fun reestablish(buffer: ByteBuffer)
}

/**
 * Calls [func] with buffer. Buffer will be got by [ByteBufferProvider.get]. And after [func] buffer
 * will return by [ByteBufferProvider.reestablish]
 */
inline fun <T> ByteBufferProvider.using(func: (ByteBuffer) -> T): T {
    val b = get()
    return try {
        func(b)
    } finally {
        reestablish(b)
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> ByteBuffer.length(length: Int, func: (ByteBuffer) -> T): T {
    contract {
        callsInPlace(func)
    }
    val l = limit
    try {
        limit = position + length
        return func(this)
    } finally {
        limit = l
    }
}
