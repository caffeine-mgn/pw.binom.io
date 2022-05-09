package pw.binom.io

import pw.binom.AsyncInput
import pw.binom.ByteBuffer

class AsyncInputWithLength(val input: AsyncInput, val length: Long) : AsyncInput {
    private var l = length
    val remaining
        get() = l
    override val available: Int
        get() = if (input.available > 0) {
            minOf(input.available, l.toInt())
        } else {
            input.available
        }

    override suspend fun read(dest: ByteBuffer): Int {
        val limit = minOf(dest.remaining, l.toInt())
        val l = dest.limit
        dest.limit = dest.position + limit
        val read = input.read(dest)
        this.l -= read
        dest.limit = l
        return read
    }

    override suspend fun asyncClose() {
        input.asyncClose()
    }
}

/**
 * Returns new AsyncInput with limit for reading bytes
 *
 * @param max limit for read
 * @return new AsyncInput with limit
 */
fun AsyncInput.withLimit(max: Long): AsyncInputWithLength {
    if (this is AsyncInputWithLength && this.length < max) {
        return this
    }
    return AsyncInputWithLength(this, max)
}
