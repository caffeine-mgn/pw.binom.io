package pw.binom.flux

import kotlinx.serialization.KSerializer
import pw.binom.io.http.Headers

object FluxServerSerializationStab : FluxServerSerialization {
    override suspend fun <T : Any> encode(
        request: FluxHttpRequest,
        value: T,
        serializer: KSerializer<T>,
        headers: Headers,
        statusCode: Int?
    ) {
        throw IllegalStateException("Serialization not supported")
    }

    override suspend fun <T : Any> decode(request: FluxHttpRequest, serializer: KSerializer<T>): T {
        throw IllegalStateException("Serialization not supported")
    }
}
