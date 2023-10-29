package pw.binom.http.rest.endpoints

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import pw.binom.url.PathMask

interface Endpoint2<REQUEST : Any, RESPONSE : Any> {
  @OptIn(InternalSerializationApi::class)
  companion object {
    fun <INPUT : Any, OUTPUT : Any> of(
      method: String,
      path: PathMask,
      input: KSerializer<INPUT>,
      output: KSerializer<OUTPUT>,
    ) =
      object : Endpoint2<INPUT, OUTPUT> {
        override val path: PathMask
          get() = path
        override val method: String
          get() = method
        override val request: KSerializer<INPUT>
          get() = input
        override val response: KSerializer<OUTPUT>
          get() = output
      }

    inline fun <reified INPUT : Any, reified OUTPUT : Any> of(method: String, path: PathMask) = of(
      method = method,
      path = path,
      input = INPUT::class.serializer(),
      output = OUTPUT::class.serializer(),
    )
  }

  val path: PathMask
  val method: String
  val request: KSerializer<REQUEST>
  val response: KSerializer<RESPONSE>
  fun copy(path: PathMask = this.path, method: String = this.method): Endpoint2<REQUEST, RESPONSE> = of(
    path = path,
    method = method,
    input = request,
    output = response,
  )
}
