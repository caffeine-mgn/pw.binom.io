package pw.binom.io.http

import pw.binom.io.Available
import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize

object AsyncEmptyHttpInput : AsyncHttpInput {
    override val isEof: Boolean
        get() = true

    override val available: Available
        get() = Available.NOT_AVAILABLE

    override suspend fun read(dest: ByteBuffer)= DataTransferSize.EMPTY

    override suspend fun asyncClose() {
    }
}
