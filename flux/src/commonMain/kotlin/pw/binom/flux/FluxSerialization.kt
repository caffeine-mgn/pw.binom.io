package pw.binom.flux

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import kotlin.coroutines.cancellation.CancellationException

interface FluxSerialization {
    fun isBodySupported(mimeType: String): Boolean

    fun isStringMapSupported(): Boolean

    fun getDefaultMimeType(inputMimeType: String): String {
        if (isBodySupported(inputMimeType)) {
            return inputMimeType
        }
        throw SerializationNotSupported()
    }

    @Throws(SerializationNotSupported::class)
    fun <T> encodeMap(
        value: T,
        serializer: KSerializer<T>,
        output: MutableMap<String, String>,
    ): Unit = throw SerializationNotSupported()

    @Throws(SerializationNotSupported::class)
    fun <T> decodeMap(serializer: KSerializer<T>, input: Map<String, String>): T =
        throw SerializationNotSupported()

    @Throws(SerializationNotSupported::class, CancellationException::class)
    suspend fun <T> encodeBody(
        mimeType: String,
        value: T,
        serializer: KSerializer<T>,
        output: AsyncOutput,
    ): Unit = throw SerializationNotSupported()

    @Throws(SerializationNotSupported::class, CancellationException::class)
    suspend fun <T> decodeBody(mimeType: String, serializer: KSerializer<T>, input: AsyncInput): T =
        throw SerializationNotSupported()

    class SerializationNotSupported : SerializationException {
        constructor() : super()
        constructor(message: String?) : super(message)
        constructor(message: String?, cause: Throwable?) : super(message, cause)
        constructor(cause: Throwable?) : super(cause)
    }
}
