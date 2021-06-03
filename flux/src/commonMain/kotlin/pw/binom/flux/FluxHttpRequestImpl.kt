package pw.binom.flux

import kotlinx.serialization.KSerializer
import pw.binom.io.httpServer.HttpRequest

class FluxHttpRequestImpl(val mask: String, val serialization: Serialization, request: HttpRequest) : FluxHttpRequest,
    HttpRequest by request {
    override val pathVariables: Map<String, String> by lazy {
        path.getVariables(mask = mask)
    }
    override val queryVariables: Map<String, List<String?>> by lazy {
        query?.toMap() ?: emptyMap()
    }

    override suspend fun <T : Any> readRequest(serializer: KSerializer<T>): T =
        serialization.decode(
            request = this,
            serializer = serializer
        )

    override suspend fun <T : Any> writeResponse(serializer: KSerializer<T>, value: T) {
//        var r = response
//        if (r == null) {
//            r = response()
//            r.status = 200
//        }
        try {
            serialization.encode(
                request = this,
                value = value,
                serializer = serializer
            )
        } finally {
            response?.asyncClose()
        }
    }
}