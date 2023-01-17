package pw.binom.io

class AsyncInputWithLength(val input: AsyncInput, val length: Long) : AsyncInput {
    init {
        require(length >= 0) { "Length should be more or equals than 0" }
    }

    private var internalRemaining = length
    val remaining
        get() = internalRemaining
    override val available: Int
        get() = if (input.available > 0) {
            minOf(input.available, internalRemaining.toInt())
        } else {
            input.available
        }

    override suspend fun read(dest: ByteBuffer): Int {
        if (dest.remaining == 0) {
            return 0
        }
        if (internalRemaining == 0L) {
            return 0
        }
        val limit = minOf(dest.remaining, internalRemaining.toInt())
        val l = dest.limit
        dest.limit = dest.position + limit
        val read = input.read(dest)
        this.internalRemaining -= read
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
    if (this is AsyncInputWithLength && this.length <= max) {
        return this
    }
    return AsyncInputWithLength(input = this, length = max)
}
