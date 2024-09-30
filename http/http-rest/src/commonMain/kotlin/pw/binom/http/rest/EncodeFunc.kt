package pw.binom.http.rest

import kotlinx.serialization.SerializationStrategy
import pw.binom.io.http.HttpOutput

@Suppress("NOTHING_TO_INLINE")
interface EncodeFunc<OUTPUT, DATA> {
  suspend fun send(data: DATA, output: HttpOutput)
  fun encode(serializer: SerializationStrategy<OUTPUT>, value: OUTPUT, output: HttpOutput): DATA

  companion object {
    private object NotSupported : EncodeFunc<Any?, Any?> {
      private inline fun throwNotSupported(): Nothing = throw IllegalStateException("Not supported")
      override suspend fun send(data: Any?, output: HttpOutput) {
        throwNotSupported()
      }

      override fun encode(serializer: SerializationStrategy<Any?>, value: Any?, output: HttpOutput): Any? {
        throwNotSupported()
      }
    }

    @Suppress("UNCHECKED_CAST")
    fun <INPUT, OUTPUT> notSupported() = NotSupported as EncodeFunc<INPUT, OUTPUT>
  }
}
