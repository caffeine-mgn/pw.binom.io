@file:JvmName("ByteBufferCommon")

package pw.binom

import pw.binom.io.Closeable
import pw.binom.io.UTF8
import pw.binom.pool.DefaultPool
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.random.Random

/**
 * A part of memory. Also contents current read/write state
 */
expect class ByteBuffer : Input, Output, Closeable, Buffer {
    companion object {
        fun alloc(size: Int): ByteBuffer
    }

    fun realloc(newSize: Int): ByteBuffer
    fun skip(length: Long): Long
    fun get(): Byte
    fun put(value: Byte)
    fun get(dest: ByteArray, offset: Int = 0, length: Int = dest.size - offset): Int

    /**
     * Returns last byte. Work as [get] but don'tm move position when he reads
     */
    fun peek(): Byte
    fun reset(position: Int, length: Int): ByteBuffer
    fun write(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): Int
    operator fun get(index: Int): Byte
    operator fun set(index: Int, value: Byte)
    fun toByteArray(): ByteArray
    fun subBuffer(index: Int, length: Int): ByteBuffer
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

fun ByteBuffer.writeShort(value: Short): ByteBuffer {
    value.dump(this)
    return this
}

fun ByteBuffer.writeInt(value: Int): ByteBuffer {
    value.dump(this)
    return this
}

fun ByteBuffer.writeLong(value: Long): ByteBuffer {
    value.dump(this)
    return this
}

fun ByteBuffer.readShort() = Short.fromBytes(this)
fun ByteBuffer.readInt() = Int.fromBytes(this)
fun ByteBuffer.readLong() = Long.fromBytes(this)

/**
 * The Function make copy of [data] to new [ByteBuffer] and then return it.
 * Returns ByteBuffer with [ByteBuffer.position]=0 and [ByteBuffer.limit]=[data].size
 *
 * @param data source data
 * @return new [ByteBuffer].
 */
fun ByteBuffer.Companion.wrap(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): ByteBuffer {
    val out = alloc(length)
    out.write(
        data = data,
        offset = offset,
        length = length
    )
    out.clear()
    return out
}

/**
 * Makes ByteBuffer from [this] String. Returns new clean ByteBuffer
 */
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

class ByteBufferPool(capacity: Int, size: UInt = DEFAULT_BUFFER_SIZE.toUInt()) :
    DefaultPool<ByteBuffer>(capacity, { ByteBuffer.alloc(size.toInt()) }), Closeable {
    override fun borrow(init: ((ByteBuffer) -> Unit)?): ByteBuffer {
        val buf = super.borrow(init)
        buf.clear()
        init?.invoke(buf)
        return buf
    }

    override fun close() {
        pool.indices.forEach {
            val element = pool[it] as? ByteBuffer?
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

inline fun ByteBuffer.forEachIndexed(func: (index: Int, value: Byte) -> Unit) {
    val pos = position
    val lim = limit
    for (it in pos until lim)
        func(it, this[it])
}

inline fun ByteBuffer.forEach(func: (Byte) -> Unit) {
    val pos = position
    val lim = limit
    for (it in pos until lim)
        func(this[it])
}

inline fun <T> ByteBuffer.map(func: (Byte) -> T): List<T> {
    val pos = position
    val lim = limit
    val output = ArrayList<T>(remaining)
    for (it in pos until lim)
        output += func(this[it])
    return output
}

fun ByteArray.input() = ByteBuffer.wrap(this)