package pw.binom.io.httpServer

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer

@Deprecated(message = "Will be removed")
internal class NoCloseOutputDeprecated(val func: (NoCloseOutputDeprecated) -> Unit) : AsyncOutput {

    var stream: AsyncOutput? = null

//    override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int =
//            stream!!.write(data, offset, length)

    override suspend fun write(data: ByteBuffer): Int =
            stream!!.write(data)

    override suspend fun flush() {
        stream!!.flush()
    }

    override suspend fun asyncClose() {
        func(this)
    }
}