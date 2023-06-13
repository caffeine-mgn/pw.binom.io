package pw.binom.flux.client

import pw.binom.io.httpClient.HttpClient
import pw.binom.url.URL

//interface RestClient : HttpClient {
//    override suspend fun connect(method: String, uri: URL): RestRequest
//
//    companion object {
//        fun create(client: HttpClient, serialization: RestClientSerialization): RestClient =
//            RestClientImpl(
//                client = client,
//                serialization = serialization,
//            )
//    }
//}
