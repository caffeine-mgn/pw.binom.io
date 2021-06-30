package pw.binom.flux

import kotlinx.serialization.KSerializer
import pw.binom.io.http.EmptyHeaders
import pw.binom.io.http.Headers
import pw.binom.io.httpServer.HttpRequest

interface FluxHttpRequest : HttpRequest {
    val pathVariables: Map<String, String>
    val queryVariables: Map<String, List<String?>>
    suspend fun <T : Any> readRequest(serializer: KSerializer<T>): T
    suspend fun <T : Any> finishResponse(
        serializer: KSerializer<T>,
        value: T,
        headers: Headers = EmptyHeaders,
        statusCode: Int? = null
    )
}