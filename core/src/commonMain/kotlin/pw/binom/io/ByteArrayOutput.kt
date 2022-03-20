package pw.binom.io

import pw.binom.ByteBuffer
import pw.binom.Output
import pw.binom.atomic.AtomicReference
import kotlin.math.ceil

open class ByteArrayOutput(capacity: Int = 512, val capacityFactor: Float = 1.7f) : Output {
    var data by AtomicReference(ByteBuffer.alloc(capacity))
        private set
    private var _wrote = 0
    private var closed = false
    private var finished = false

    /**
     * Returns current size of buffer. Buffer can be grown if you call [alloc] or write date more than [capacity]
     */
    val capacity
        get() = data.capacity

    fun clear() {
        _wrote = 0
        data.clear()
        finished = false
    }

    fun trimToSize() {
        checkLocked()
        if (data.capacity != _wrote) {
            val old = this.data
            this.data = this.data.realloc(_wrote)
            old.close()
        }
    }

    fun toByteArray(): ByteArray {
        checkLocked()
        val position = data.position
        val limit = data.limit
        try {
            data.flip()
            return data.toByteArray()
        } finally {
            data.limit = limit
            data.position = position
        }
    }

    private inline fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }

    fun writeByte(byte: Byte) {
        checkLocked()
        alloc(1)
        data.put(byte)
        _wrote++
    }

    fun alloc(size: Int) {
        checkLocked()
        checkClosed()

        val needWrite = size - (this.data.remaining)

        if (needWrite > 0) {
            val newSize = maxOf(
                ceil(this.data.capacity.let { if (it == 0) 1 else it } * capacityFactor).toInt(),
                this.data.capacity + _wrote + needWrite
            )
            val old = this.data
            val new = this.data.realloc(newSize)
            new.limit = new.capacity
            this.data = new
            old.close()
        }
    }

    override fun write(data: ByteBuffer): Int {
        checkLocked()
        alloc(data.remaining)
        val l = this.data.write(data)
        _wrote += l
        return l
    }

    fun write(data: ByteArray): Int {
        checkLocked()
        alloc(data.size)
        val l = this.data.write(data)
        _wrote += l
        return l
    }

    private fun checkLocked() {
        if (finished) {
            throw IllegalStateException("ByteBuffer finished")
        }
    }

    override fun flush() {
        // Do nothing
    }

    override fun close() {
        checkLocked()
        checkClosed()
        data.close()
        closed = true
    }

    val size: Int
        get() = _wrote

    fun unlock(position: Int) {
        clear()
        _wrote = position
        data.limit = data.capacity
        data.position = position
    }

    /**
     * Lock this ByteBuffer and return [ByteBuffer] with data. Limit and offset sets for actial data in this [ByteBuffer].
     * After call this function you can't modify this storage using his methods.
     * For modify this storage you should call [clear] or [unlock]
     */
    fun lock(): ByteBuffer {
        checkLocked()
        finished = true
        data.flip()
        return data
    }

    inline fun <T> locked(func: (ByteBuffer) -> T): T {
        val oldPosition = data.position
        try {
            return func(lock())
        } finally {
            unlock(oldPosition)
        }
    }
}
