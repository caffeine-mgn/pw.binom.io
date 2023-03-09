package pw.binom.flux

import kotlinx.serialization.KSerializer
import pw.binom.io.http.Headers
import pw.binom.io.http.emptyHeaders
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.url.PathMask

class FluxHttpServerExchange(
    val fluxSerialization: AbstractCommonFluxSerialization,
    val pathMask: PathMask,
    val source: HttpServerExchange,
) : HttpServerExchange by source {
    val queryVariables by lazy {
        getQueryParams()
    }
    val pathVariables by lazy {
        source.requestURI.path.getVariables(pathMask) ?: emptyMap()
    }

    suspend fun <T> read(serializer: KSerializer<T>): T =
        fluxSerialization.read(exchange = source, serializer = serializer)

    suspend fun <T> response(
        serializer: KSerializer<T>,
        value: T,
        status: Int = 200,
        headers: Headers = emptyHeaders(),
    ) {
        fluxSerialization.response(
            status = status,
            headers = headers,
            exchange = source,
            value = value,
            serializer = serializer,
        )
    }
}
