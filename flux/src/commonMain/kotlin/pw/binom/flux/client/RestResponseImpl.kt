package pw.binom.flux.client

import kotlinx.serialization.KSerializer
import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncInput
import pw.binom.io.AsyncReader
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.HttpResponse

internal class RestResponseImpl(val resp: HttpResponse, val serialization: RestClientSerialization) : RestResponse {
    override suspend fun <T : Any> readObject(serializer: KSerializer<T>): T =
        serialization.decode(
            request = this,
            serializer = serializer,
        )

    override val headers: Headers
        get() = resp.headers
    override val responseCode: Int
        get() = resp.responseCode

    override suspend fun asyncClose() {
        resp.asyncClose()
    }

    override suspend fun readData(): AsyncInput = resp.readData()

    override suspend fun readText(): AsyncReader = resp.readText()

    override suspend fun startTcp(): AsyncChannel = resp.startTcp()
}
