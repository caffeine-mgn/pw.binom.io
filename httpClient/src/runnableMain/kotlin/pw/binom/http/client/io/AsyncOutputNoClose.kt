package pw.binom.http.client.io

import pw.binom.io.AsyncOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.ClosedException
import pw.binom.io.DataTransferSize

internal class AsyncOutputNoClose(private val source: AsyncOutput) : AsyncOutput {
    private var closed = false
    fun markClosed() {
        closed = true
    }

    override suspend fun write(data: ByteBuffer): DataTransferSize {
        if (closed) {
            return DataTransferSize.CLOSED
        }
        return source.write(data)
    }

    override suspend fun write(data: ByteArray, offset: Int, length: Int): DataTransferSize {
        if (closed) {
            return DataTransferSize.CLOSED
        }
        return source.write(data, offset, length)
    }

    override suspend fun writeByte(value: Byte) {
        if (closed) {
            throw ClosedException()
        }
        source.writeByte(value)
    }

    override suspend fun writeDouble(value: Double) {
        if (closed) {
            throw ClosedException()
        }
        source.writeDouble(value)
    }

    override suspend fun writeFloat(value: Float) {
        if (closed) {
            throw ClosedException()
        }
        source.writeFloat(value)
    }

    override suspend fun writeFully(data: ByteArray, offset: Int, length: Int) {
        if (closed) {
            throw ClosedException()
        }
        source.writeFully(data, offset, length)
    }

    override suspend fun writeFully(data: ByteBuffer): Int {
        if (closed) {
            throw ClosedException()
        }
        return source.writeFully(data)
    }

    override suspend fun writeInt(value: Int) {
        if (closed) {
            throw ClosedException()
        }
        source.writeInt(value)
    }

    override suspend fun writeLong(value: Long) {
        if (closed) {
            throw ClosedException()
        }
        source.writeLong(value)
    }

    override suspend fun writeShort(value: Short) {
        if (closed) {
            throw ClosedException()
        }
        source.writeShort(value)
    }

    override suspend fun writeString(value: String) {
        if (closed) {
            throw ClosedException()
        }
        source.writeString(value)
    }

    override suspend fun asyncClose() {
        if (!closed) {
            source.flush()
        }
    }

    override suspend fun flush() {
        if (closed) {
            source.flush()
        }
    }
}
