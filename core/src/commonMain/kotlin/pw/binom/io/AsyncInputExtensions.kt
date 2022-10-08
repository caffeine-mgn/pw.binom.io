package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.copyTo

suspend fun AsyncInput.readBytes(bufferProvider: ByteBufferProvider) = bufferProvider.using { buffer ->
    readBytes(buffer)
}

suspend fun AsyncInput.readBytes(bufferSize: Int = DEFAULT_BUFFER_SIZE) = ByteBuffer.alloc(bufferSize).use { buffer ->
    readBytes(buffer)
}

suspend fun AsyncInput.readBytes(buffer: ByteBuffer) = ByteArrayOutput().use { out ->
    this.copyTo(out, buffer)
    out.toByteArray()
}
