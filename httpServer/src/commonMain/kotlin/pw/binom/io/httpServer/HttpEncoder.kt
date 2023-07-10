package pw.binom.io.httpServer

import pw.binom.io.http.MutableHeaders
import kotlin.reflect.KClass

interface HttpEncoder<T : Any> {
  suspend fun decodeQuery(key: String, value: String?, type: KClass<T>, exchange: HttpServerExchange): T
  suspend fun decodePath(key: String, value: String, type: KClass<T>, exchange: HttpServerExchange): T
  suspend fun decodeRequestBody(exchange: HttpServerExchange, type: KClass<T>): T
  suspend fun encodeResponseBody(value: T, headers: MutableHeaders, type: KClass<T>, exchange: HttpServerExchange)
}
