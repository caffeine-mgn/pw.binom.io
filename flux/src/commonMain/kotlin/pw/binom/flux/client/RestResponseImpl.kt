package pw.binom.flux.client

import kotlinx.serialization.KSerializer
import pw.binom.io.AsyncInput
import pw.binom.io.AsyncReader
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.HttpResponse
import pw.binom.url.Path
import pw.binom.url.Query

internal class RestResponseImpl(val resp: HttpResponse, val serialization: RestClientSerialization) : RestResponse {
    override suspend fun <T : Any> readObject(serializer: KSerializer<T>): T =
        serialization.decode(
            request = this,
            serializer = serializer,
        )

    override val inputHeaders: Headers
        get() = resp.inputHeaders
    override val responseCode: Int
        get() = resp.responseCode

    override suspend fun asyncClose() {
        resp.asyncClose()
    }

    override val path: Path
        get() = resp.path
    override val query: Query?
        get() = resp.query

    override suspend fun readBinary(): AsyncInput = resp.readBinary()

    override suspend fun readText(): AsyncReader = resp.readText()

//    override suspend fun startTcp(): AsyncChannel = resp.startTcp()
}
