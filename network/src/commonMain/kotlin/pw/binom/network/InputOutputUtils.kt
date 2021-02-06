package pw.binom.network

import pw.binom.*
import pw.binom.pool.ObjectPool

suspend fun Input.copyToAsync(output: AsyncOutput, tempBuffer: ByteBuffer): Long {
    var totalLength = 0L
    tempBuffer.doFreeze()
    while (true) {
        tempBuffer.clear()
        val length = execute { read(tempBuffer) }
        if (length == 0)
            break
        totalLength += length.toLong()
        tempBuffer.flip()
        network {
            output.write(tempBuffer)
        }
    }
    return totalLength
}

suspend fun AsyncInput.copyToAsync(output: Output, tempBuffer: ByteBuffer): Long {
    var totalLength = 0L
    tempBuffer.doFreeze()
    while (true) {
        tempBuffer.clear()
        val length = network { read(tempBuffer) }
        if (length == 0)
            break
        totalLength += length.toLong()
        tempBuffer.flip()
        execute {
            output.write(tempBuffer)
        }
    }
    return totalLength
}

suspend fun AsyncInput.copyToAsync(output: Output, pool: ObjectPool<ByteBuffer>): Long {
    val buffer = pool.borrow()
    try {
        return copyToAsync(output, buffer)
    } finally {
        pool.recycle(buffer)
    }
}

suspend fun AsyncInput.copyToAsync(output: Output, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long {
    val buffer = ByteBuffer.alloc(bufferSize)
    try {
        return copyToAsync(output, buffer)
    } finally {
        buffer.close()
    }
}