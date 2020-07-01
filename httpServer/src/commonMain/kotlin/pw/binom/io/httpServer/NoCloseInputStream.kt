package pw.binom.io.httpServer

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.ByteDataBuffer
import pw.binom.io.AsyncInputStream

internal class NoCloseInputStream : AsyncInputStream {

    var stream: AsyncInputStream? = null

    override suspend fun read(): Byte =
            stream!!.read()

    override suspend fun skip(length: Long): Long =
            stream!!.skip(length)

    override suspend fun read(data: ByteArray, offset: Int, length: Int): Int =
            stream!!.read(data, offset, length)

    override suspend fun close() {
    }
}

internal class NoCloseInput : AsyncInput {

    var stream: AsyncInput? = null

    override suspend fun skip(length: Long): Long =
            stream!!.skip(length)

//    override suspend fun read(data: ByteDataBuffer, offset: Int, length: Int): Int =
//            stream!!.read(data, offset, length)

    override suspend fun read(dest: ByteBuffer): Int =
            stream!!.read(dest)

    override suspend fun close() {
    }
}