package pw.binom.io.http

import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize

object AsyncEmptyHttpInput : AsyncHttpInput {
    override val isEof: Boolean
        get() = true

    override val available: Int
        get() = 0

    override suspend fun read(dest: ByteBuffer)= DataTransferSize.EMPTY

    override suspend fun asyncClose() {
    }
}
