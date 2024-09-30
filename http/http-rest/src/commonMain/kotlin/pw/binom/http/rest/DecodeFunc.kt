package pw.binom.http.rest

import kotlinx.serialization.DeserializationStrategy
import pw.binom.io.http.HttpInput

@Suppress("NOTHING_TO_INLINE")
interface DecodeFunc<TYPE, DATA> {
  suspend fun read(input: HttpInput): DATA
  fun decode(serializer: DeserializationStrategy<TYPE>, data: DATA, input: HttpInput): TYPE

  companion object {
    private object NotSupported : DecodeFunc<Any?, Any?> {
      private inline fun throwNotSupported(): Nothing = throw IllegalStateException("Not supported")
      override suspend fun read(input: HttpInput): Any? {
        throwNotSupported()
      }

      override fun decode(serializer: DeserializationStrategy<Any?>, data: Any?, input: HttpInput): Any? {
        throwNotSupported()
      }
    }

    @Suppress("UNCHECKED_CAST")
    fun <TYPE, DATA> notSupported() = NotSupported as DecodeFunc<TYPE, DATA>
  }
}
