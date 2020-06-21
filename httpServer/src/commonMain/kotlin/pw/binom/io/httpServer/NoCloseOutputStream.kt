package pw.binom.io.httpServer

import pw.binom.AsyncOutput
import pw.binom.ByteDataBuffer
import pw.binom.io.AsyncOutputStream

internal class NoCloseOutputStream(val func: (NoCloseOutputStream) -> Unit) : AsyncOutputStream {

    var stream: AsyncOutputStream? = null

    override suspend fun write(data: Byte): Boolean = stream!!.write(data)

    override suspend fun write(data: ByteArray, offset: Int, length: Int): Int =
            stream!!.write(data, offset, length)

    override suspend fun flush() {
        stream!!.flush()
    }

    override suspend fun close() {
        func(this)
    }
}

internal class NoCloseOutput(val func: (NoCloseOutput) -> Unit) : AsyncOutput {

    var stream: AsyncOutput? = null

    override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int =
            stream!!.write(data, offset, length)

    override suspend fun flush() {
        stream!!.flush()
    }

    override suspend fun close() {
        func(this)
    }
}