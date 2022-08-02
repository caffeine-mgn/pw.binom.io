package pw.binom.flux

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import pw.binom.io.http.EmptyHeaders
import pw.binom.io.http.Headers
import pw.binom.io.httpServer.HttpRequest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

interface FluxHttpRequest : HttpRequest, CoroutineContext {
    companion object {
        suspend fun getActiveRequestOrNull(): FluxHttpRequest? = coroutineContext[FluxHttpRequestImplKey]
        suspend fun getActiveRequest() =
            getActiveRequestOrNull() ?: throw IllegalStateException("No active FluxHttpRequest")
    }

    val pathVariables: Map<String, String>
    val queryVariables: Map<String, String?>
    suspend fun <T : Any> readRequest(serializer: KSerializer<T>): T
    suspend fun <T : Any> finishResponse(
        serializer: KSerializer<T>,
        value: T,
        headers: Headers = EmptyHeaders,
        statusCode: Int? = null
    )
}

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> FluxHttpRequest.finishResponse(
    value: T,
    headers: Headers = EmptyHeaders,
    statusCode: Int? = null
) = finishResponse(value = value, headers = headers, statusCode = statusCode, serializer = T::class.serializer())
