package pw.binom.io.httpServer

import pw.binom.io.AsyncOutput
import pw.binom.io.AsyncWriter
import pw.binom.io.http.EmptyReadOnlyMutableHeaders
import pw.binom.io.http.MutableHeaders

object StubHttpResponse : HttpResponse {
    private fun throwError(): Nothing = throw IllegalStateException("Can't update stub response")
    override var status: Int
        get() = 0
        set(value) {
            throwError()
        }
    override val headers: MutableHeaders
        get() = EmptyReadOnlyMutableHeaders

    override suspend fun startWriteBinary(): AsyncOutput = throwError()

    override suspend fun startWriteText(): AsyncWriter = throwError()

    override suspend fun asyncClose() {
    }
}
