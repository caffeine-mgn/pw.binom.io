package pw.binom.http.rest.endpoints

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

interface Endpoint2<REQUEST : Any, RESPONSE : Any> {
  @OptIn(InternalSerializationApi::class)
  companion object {
    fun <INPUT : Any, OUTPUT : Any> of(input: KSerializer<INPUT>, output: KSerializer<OUTPUT>) =
      object : Endpoint2<INPUT, OUTPUT> {
        override val request: KSerializer<INPUT>
          get() = input
        override val response: KSerializer<OUTPUT>
          get() = output
      }

    inline fun <reified INPUT : Any, reified OUTPUT : Any> of() = of(
      input = INPUT::class.serializer(),
      output = OUTPUT::class.serializer(),
    )
  }

  val request: KSerializer<REQUEST>
  val response: KSerializer<RESPONSE>
}
