@file:JvmName("ByteBufferCommon")

package pw.binom

import pw.binom.collections.defaultMutableList
import pw.binom.io.ByteBuffer
import pw.binom.io.ByteBufferProvider
import pw.binom.io.Closeable
import pw.binom.io.UTF8
import pw.binom.pool.AbstractFixedSizePool
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName

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
fun ByteBuffer.readFloat() = Float.fromBits(readInt())
fun ByteBuffer.readDouble() = Double.fromBits(readLong())

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
    val len = sumOf {
        UTF8.unicodeToUtf8Size(it)
    }
    val buf = ByteBuffer.alloc(len)
    UTF8.unicodeToUtf8(this, buf)
    buf.clear()
    return buf
}

class ByteBufferPool(capacity: Int, val bufferSize: UInt = DEFAULT_BUFFER_SIZE.toUInt()) :
    AbstractFixedSizePool<ByteBuffer>(capacity), ByteBufferAllocator, ByteBufferProvider, Closeable {
    override fun new(): ByteBuffer = ByteBuffer.alloc(bufferSize.toInt())

    override fun free(value: ByteBuffer) {
        value.close()
    }

    override fun get(): ByteBuffer = borrow()

    override fun reestablish(buffer: ByteBuffer) {
        recycle(buffer)
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> ByteBuffer.set(position: Int, length: Int, func: (ByteBuffer) -> T): T {
    contract {
        callsInPlace(func, InvocationKind.EXACTLY_ONCE)
    }
    val oldLimit = this.limit
    val oldPosition = this.position
    try {
        this.position = position
        limit = position + length
        return func(this)
    } finally {
        this.limit = oldLimit
        this.position = oldPosition
    }
}

inline fun <T> ByteBuffer.map(func: (Byte) -> T): List<T> {
    val pos = position
    val lim = limit
    val output = defaultMutableList<T>(remaining)
    for (it in pos until lim)
        output += func(this[it])
    return output
}

fun ByteBuffer.asUTF8String(): String {
    if (remaining == 0) {
        return ""
    }
    val sb = StringBuilder(remaining)
    while (remaining > 0) {
        val first = getByte()
        sb.append(UTF8.utf8toUnicode(first, this))
    }
    return sb.toString()
}
