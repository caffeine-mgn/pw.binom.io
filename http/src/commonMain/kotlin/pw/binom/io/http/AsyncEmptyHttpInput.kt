package pw.binom.io.http

import pw.binom.io.ByteBuffer

object AsyncEmptyHttpInput : AsyncHttpInput {
    override val isEof: Boolean
        get() = true

    override val available: Int
        get() = 0

    override suspend fun read(dest: ByteBuffer): Int = 0

    override suspend fun asyncClose() {
    }
}
