@file:JvmName("ByteBufferCommon")

package pw.binom

import pw.binom.io.Closeable
import pw.binom.io.UTF8
import pw.binom.pool.DefaultPool
import pw.binom.pool.ObjectPool
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.random.Random

expect class ByteBuffer : Input, Output, Closeable {
    companion object {
        fun alloc(size: Int): ByteBuffer
    }

    fun realloc(newSize: Int): ByteBuffer
    fun flip()
    val remaining: Int
    var position: Int
    var limit: Int
    val capacity: Int
    fun skip(length: Long): Long
    fun get(): Byte
    fun put(value: Byte)
    fun reset(position: Int, length: Int): ByteBuffer
    fun clear()
    operator fun get(index: Int): Byte
    operator fun set(index: Int, value: Byte)
}

inline fun ByteBuffer.clone() = realloc(capacity)

/**
 * Puts random bytes to free space of [data]
 */
fun Random.nextBytes(data: ByteBuffer) {
    repeat(data.remaining) {
        data.put(nextInt().toByte())
    }
}

/**
 * The Function make copy of [data] to new [ByteBuffer] and then return it.
 * Returns ByteBuffer with [ByteBuffer.position]=0 and [ByteBuffer.limit]=[data].size
 *
 * @param data source data
 * @return new [ByteBuffer].
 */
fun ByteBuffer.Companion.wrap(data: ByteArray): ByteBuffer {
    val out = ByteBuffer.alloc(data.size)
    data.forEach {
        out.put(it)
    }
    out.clear()
    return out
}

fun String.toByteBufferUTF8(): ByteBuffer {
    val len = sumBy {
        UTF8.unicodeToUtf8Size(it)
    }
    val buf = ByteBuffer.alloc(len)
    UTF8.unicodeToUtf8(this, buf)
    buf.clear()
    return buf
}

fun ByteBuffer.empty(): ByteBuffer {
    position = 0
    limit = 0
    return this
}

class ByteBufferPool(size: Int = DEFAULT_BUFFER_SIZE) : DefaultPool<ByteBuffer>(10, { ByteBuffer.alloc(size) }), Closeable {
    override fun borrow(init: ((ByteBuffer) -> Unit)?): ByteBuffer {
        val buf = super.borrow(init)
        buf.clear()
        init?.invoke(buf)
        return buf
    }

    override fun close() {
        pool.indices.forEach {
            val element = pool[it]
            if (element != null) {
                element.close()
                pool[it] = null
            }
        }
        this.size = 0
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> pw.binom.ByteBuffer.length(length: Int, func: (pw.binom.ByteBuffer) -> T): T {
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

@OptIn(ExperimentalContracts::class)
inline fun <T> pw.binom.ByteBuffer.set(position: Int, length: Int, func: (pw.binom.ByteBuffer) -> T): T {
    contract {
        callsInPlace(func)
    }
    val l = limit
    val o = position
    try {
        this.position = position
        limit = position + length
        return func(this)
    } finally {
        limit = l
    }
}