package pw.binom.flux.client

import pw.binom.io.httpClient.HttpClient
import pw.binom.net.URL

internal class RestClientImpl(val client: HttpClient, val serialization: RestClientSerialization) : RestClient {
    override fun close() {
        client.close()
    }

    override suspend fun connect(method: String, uri: URL): RestRequest {
        val req = client.connect(method = method, uri = uri)
        return RestRequestImpl(request = req, serialization = serialization)
    }
}
