package pw.binom.flux

import kotlinx.serialization.KSerializer
import pw.binom.io.httpServer.HttpRequest

interface FluxHttpRequest : HttpRequest {
    fun getPathVariable(name: String): String?
    fun getPathVariables(): Map<String, String>
    suspend fun <T : Any> readRequest(serializer: KSerializer<T>): T
    suspend fun <T : Any> writeResponse(serializer: KSerializer<T>, value: T)
}