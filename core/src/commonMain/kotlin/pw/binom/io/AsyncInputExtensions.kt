package pw.binom.io

import pw.binom.ByteBufferPool
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.copyTo
import pw.binom.pool.using

suspend fun AsyncInput.readBytes(bufferProvider: ByteBufferPool) = bufferProvider.using { buffer ->
    readBytes(buffer)
}

suspend fun AsyncInput.readBytes(bufferSize: Int = DEFAULT_BUFFER_SIZE) = ByteBuffer(bufferSize).use { buffer ->
    readBytes(buffer)
}

suspend fun AsyncInput.readBytes(buffer: ByteBuffer) = ByteArrayOutput().use { out ->
    this.copyTo(out, buffer)
    out.toByteArray()
}
