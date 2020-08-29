package pw.binom.io.httpClient

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.io.IOException
import pw.binom.io.http.AsyncHttpInput

/**
 * Stream for read http response. This stream is close when this stream can't read data from [stream]
 */
internal class ClosableAsyncInput(val stream: AsyncInput) : AsyncHttpInput {
    private var eof = false
    override val isEof: Boolean
        get() = eof
    override val available: Int
        get() = if (eof) 0 else stream.available

    override suspend fun read(dest: ByteBuffer): Int =
            try {
                val r = stream.read(dest)
                if (r == 0) {
                    close()
                }
                r
            } catch (e: IOException) {
                close()
                0
            }

    override suspend fun close() {
        eof = true
    }

}