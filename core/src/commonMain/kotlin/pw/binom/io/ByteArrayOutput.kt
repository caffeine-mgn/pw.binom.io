package pw.binom.io

import pw.binom.ByteDataBuffer
import pw.binom.Output
import pw.binom.copyInto
import pw.binom.realloc
import kotlin.math.ceil

class ByteArrayOutput(capacity: Int = 512, val capacityFactor: Float = 1.7f) : Output {
    var data: ByteDataBuffer = ByteDataBuffer.alloc(capacity)
        private set
    private var _wrote = 0
    private var closed = false

    fun clear() {
        _wrote = 0
    }

    fun trimToSize() {
        if (data.size != _wrote) {
            this.data = this.data.realloc(_wrote)
        }
    }

    private inline fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }

    override fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
        checkClosed()

        if (length < 0)
            throw IndexOutOfBoundsException("Length can't be less than 0")

        if (length == 0)
            return 0
        val needWrite = length - (this.data.size - _wrote)

        if (needWrite > 0) {
            val newSize = maxOf(
                    ceil(this.data.size.let { if (it == 0) 1 else it } * capacityFactor).toInt(),
                    this.data.size + _wrote + needWrite
            )
            this.data = this.data.realloc(newSize)
        }
        data.copyInto(this.data, _wrote, offset, offset + length)
        _wrote += length
        return length
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