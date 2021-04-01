package pw.binom.flux

import kotlinx.serialization.KSerializer

object SerializationStab : Serialization {
     override suspend  fun <T : Any> encode(request: FluxHttpRequest, value: T, serializer: KSerializer<T>) {
        throw IllegalStateException("Serialization not supported")
    }

    override suspend  fun <T : Any> decode(request: FluxHttpRequest, serializer: KSerializer<T>): T {
        throw IllegalStateException("Serialization not supported")
    }

}