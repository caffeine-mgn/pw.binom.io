package pw.binom.flux.client

import pw.binom.io.httpClient.HttpClient
import pw.binom.url.URL

internal class RestClientImpl(val client: HttpClient, val serialization: RestClientSerialization) : RestClient {
    override fun close() {
        client.close()
    }

    override suspend fun connect(method: String, uri: URL): RestRequest {
        val req = client.connect(method = method, uri = uri)
        return RestRequestImpl(request1 = req, serialization = serialization)
    }
}
