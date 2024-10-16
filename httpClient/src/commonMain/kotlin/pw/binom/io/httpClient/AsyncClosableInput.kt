package pw.binom.io.httpClient

import pw.binom.ByteDataBuffer
import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.io.StreamClosedException
import pw.binom.network.SocketClosedException

//@Deprecated(message = "Use HttpClient", level = DeprecationLevel.WARNING)
//class AsyncClosableInput(val stream: AsyncInput) : AsyncInput {
//
//    private var eof = false
//    private var closed = false
//    override val available: Int
//        get() = if (eof || closed) 0 else stream.available
//
//    override suspend fun read(dest: ByteBuffer): Int {
//        checkClosed()
//        if (eof)
//            return 0
//        return try {
//            stream.read(dest)
//        } catch (e: SocketClosedException) {
//            eof = true
//            0
//        }
//    }
//
//    override suspend fun asyncClose() {
//        checkClosed()
//        closed = true
//    }
//
//    private fun checkClosed() {
//        if (closed)
//            throw StreamClosedException()
//    }
//
//}