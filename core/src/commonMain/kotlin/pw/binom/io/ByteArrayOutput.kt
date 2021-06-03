package pw.binom.io

import pw.binom.ByteBuffer
import pw.binom.Output
import pw.binom.atomic.AtomicReference
import kotlin.math.ceil

class ByteArrayOutput(capacity: Int = 512, val capacityFactor: Float = 1.7f) : Output {
    var data by AtomicReference(ByteBuffer.alloc(capacity))
        private set
    private var _wrote = 0
    private var closed = false

    fun clear() {
        _wrote = 0
        data.clear()
    }

    fun trimToSize() {
        if (data.capacity != _wrote) {
            val old = this.data
            this.data = this.data.realloc(_wrote)
            old.close()
        }
    }

    fun toByteArray(): ByteArray {
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
        alloc(1)
        data.put(byte)
        _wrote++
    }

    fun alloc(size: Int) {
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
        alloc(data.remaining)
        val l = this.data.write(data)
        _wrote += l
        return l
    }

    override fun flush() {
    }

    override fun close() {
        checkClosed()
        data.close()
        closed = true
    }

    val size: Int
        get() = _wrote
}