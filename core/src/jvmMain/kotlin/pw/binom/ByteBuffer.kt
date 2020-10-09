package pw.binom

import pw.binom.io.Closeable
import pw.binom.io.StreamClosedException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import java.nio.ByteBuffer as JByteBuffer

actual class ByteBuffer(var native: JByteBuffer) : Input, Output, Closeable {
    actual companion object {
        actual fun alloc(size: Int): ByteBuffer =
                ByteBuffer(JByteBuffer.allocate(size))

        fun wrap(native: JByteBuffer) = ByteBuffer(native)
    }

    private var closed = false
    private inline fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }

    actual fun flip() {
        checkClosed()
        native.flip()
    }

    actual val remaining: Int
        get() {
            checkClosed()
            return native.remaining()
        }

    actual fun skip(length: Long): Long {
        checkClosed()
        val pos = minOf((native.position() + length).toInt(), native.limit())
        val len = pos - native.position()
        native.position(pos)
        return len.toLong()
    }

//    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        checkClosed()
//        return data.buffer.update(offset, length) { data ->
//            native.copyTo(data)
//        }
//    }

//    override fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        checkClosed()
//        return data.buffer.update(offset, length) { data ->
//            data.copyTo(native)
//        }
//    }

    override fun write(data: ByteBuffer): Int {
        if (data === this)
            throw IllegalArgumentException()
        val l = minOf(remaining, data.remaining)
        length(l) { self ->
            data.length(l) { src ->
                self.native.put(src.native)
            }
        }
        return l
    }

    override fun flush() {
        checkClosed()
    }

    override fun close() {
        checkClosed()
        native = JByteBuffer.allocate(0)
        closed = true
    }

    actual var position: Int
        get() {
            checkClosed()
            return native.position()
        }
        set(value) {
            checkClosed()
            native.position(value)
        }
    actual var limit: Int
        get() {
            checkClosed()
            return native.limit()
        }
        set(value) {
            checkClosed()
            native.limit(value)
        }

    actual val capacity: Int
        get() {
            checkClosed()
            return native.capacity()
        }

    actual operator fun get(index: Int): Byte {
        checkClosed()
        return native.get(index)
    }

    actual operator fun set(index: Int, value: Byte) {
        checkClosed()
        native.put(index, value)
    }

    override fun read(dest: ByteBuffer): Int {
        val l = minOf(remaining, dest.remaining)
        if (l == 0)
            return l
        val selfLimit = native.limit()
        val destLimit = dest.native.limit()
        native.limit(native.position() + l)
        dest.native.limit(dest.native.position() + l)
        dest.native.put(native)
        native.limit(selfLimit)
        dest.native.limit(destLimit)
        return l
    }

    actual fun get(): Byte =
            native.get()


    actual fun reset(position: Int, length: Int): ByteBuffer {
        native.position(position)
        native.limit(position + length)
        return this
    }

    actual fun put(value: Byte) {
        native.put(value)
    }

    actual fun clear() {
        native.clear()
    }

    actual fun realloc(newSize: Int): ByteBuffer {
        val new = ByteBuffer.alloc(newSize)
        if (newSize > capacity) {
            native.hold(0, capacity) { self ->
                new.native.update(0, native.capacity()) { new ->
                    new.put(self)
                }
            }
            new.position = position
            new.limit = limit
        } else {
            native.hold(0, newSize) { self ->
                new.native.update(0, newSize) { new ->
                    new.put(self)
                }
            }
            new.position = minOf(position, newSize)
            new.limit = minOf(limit, newSize)
        }
        return new
    }

    actual fun toByteArray(): ByteArray {
        val r = ByteArray(remaining)
        native.get(r)
        return r
    }

    actual fun write(data: ByteArray, offset: Int, length: Int): Int {
        val l = minOf(remaining, length)
        native.put(data, offset, length)
        return l
    }

    actual fun compact() {
        if (position == 0) {
            native.clear()
        } else {
            native.compact()
        }
    }

    actual fun peek(): Byte {
        if (position == limit) {
            throw NoSuchElementException()
        }
        return get(position)
    }

    actual fun subBuffer(index: Int, length: Int): ByteBuffer {
        val p = position
        val l = limit
        position = 0
        limit = capacity
        position = index
        limit = index + length
        val newBytes = JByteBuffer.allocate(length)
        native.copyTo(newBytes)
        return ByteBuffer(newBytes)
    }
}

@OptIn(ExperimentalContracts::class)
private inline fun <T> JByteBuffer.hold(offset: Int, length: Int, func: (java.nio.ByteBuffer) -> T): T {
    contract {
        callsInPlace(func)
    }
    val p = position()
    val l = limit()
    try {
        position(offset)
        limit(offset + length)
        return func(this)
    } finally {
        limit(l)
        position(p)
    }
}

@OptIn(ExperimentalContracts::class)
private inline fun <T> JByteBuffer.update(offset: Int, length: Int, func: (java.nio.ByteBuffer) -> T): T {
    contract {
        callsInPlace(func)
    }
    try {
        position(offset)
        limit(offset + length)
        return func(this)
    } finally {
        clear()
    }
}

private inline fun JByteBuffer.copyTo(buffer: JByteBuffer): Int {
    val l = minOf(buffer.remaining(), remaining())
    buffer.put(buffer)
    return l
}