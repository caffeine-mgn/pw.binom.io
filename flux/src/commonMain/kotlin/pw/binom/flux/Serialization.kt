package pw.binom.flux

import kotlinx.serialization.KSerializer
import pw.binom.AsyncOutput

interface Serialization {
    suspend fun <T : Any> encode(request: FluxHttpRequest, value: T, serializer: KSerializer<T>)
    suspend fun <T : Any> decode(request: FluxHttpRequest, serializer: KSerializer<T>): T
}