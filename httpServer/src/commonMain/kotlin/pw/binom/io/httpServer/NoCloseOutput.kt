package pw.binom.io.httpServer

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer

internal class NoCloseOutput(val func: (NoCloseOutput) -> Unit) : AsyncOutput {

    var stream: AsyncOutput? = null

//    override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int =
//            stream!!.write(data, offset, length)

    override suspend fun write(data: ByteBuffer): Int =
            stream!!.write(data)

    override suspend fun flush() {
        stream!!.flush()
    }

    override suspend fun close() {
        func(this)
    }
}