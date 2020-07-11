package pw.binom.io

import pw.binom.*
import kotlin.math.ceil

class ByteArrayOutput(capacity: Int = 512, val capacityFactor: Float = 1.7f) : Output {
    var data = ByteBuffer.alloc(capacity)
        private set
    private var _wrote = 0
    private var closed = false

    fun clear() {
        _wrote = 0
    }

    fun trimToSize() {
        if (data.capacity != _wrote) {
            this.data = this.data.realloc(_wrote)
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

//    override fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        checkClosed()
//
//        if (length < 0)
//            throw IndexOutOfBoundsException("Length can't be less than 0")
//
//        if (length == 0)
//            return 0
//        val needWrite = length - (this.data.size - _wrote)
//
//        if (needWrite > 0) {
//            val newSize = maxOf(
//                    ceil(this.data.size.let { if (it == 0) 1 else it } * capacityFactor).toInt(),
//                    this.data.size + _wrote + needWrite
//            )
//            this.data = this.data.realloc(newSize)
//        }
//        data.copyInto(this.data, _wrote, offset, offset + length)
//        _wrote += length
//        return length
//    }

    override fun write(data: ByteBuffer): Int {
        checkClosed()

        val needWrite = data.remaining - (this.data.remaining)

        if (needWrite > 0) {
            val newSize = maxOf(
                    ceil(this.data.capacity.let { if (it == 0) 1 else it } * capacityFactor).toInt(),
                    this.data.capacity + _wrote + needWrite
            )
            this.data.realloc(newSize)
            this.data.limit = this.data.capacity
        }
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