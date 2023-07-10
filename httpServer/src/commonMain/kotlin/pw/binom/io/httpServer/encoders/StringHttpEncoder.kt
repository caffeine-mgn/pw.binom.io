package pw.binom.io.httpServer.encoders

import pw.binom.charset.Charsets
import pw.binom.io.bufferedReader
import pw.binom.io.bufferedWriter
import pw.binom.io.http.MutableHeaders
import pw.binom.io.httpServer.HttpEncoder
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.io.use
import kotlin.reflect.KClass

object StringHttpEncoder : HttpEncoder<String> {
  override suspend fun decodeQuery(key: String, value: String?, type: KClass<String>, exchange: HttpServerExchange) =
    value ?: throw IllegalArgumentException("Query argument \"$key\" is null")

  override suspend fun decodeRequestBody(exchange: HttpServerExchange, type: KClass<String>): String {
    val charset = exchange.requestHeaders.charset?.let { Charsets.get(it) } ?: Charsets.UTF8
    return exchange.input.bufferedReader(charset = charset).use {
      it.readText()
    }
  }

  override suspend fun encodeResponseBody(
    value: String,
    headers: MutableHeaders,
    type: KClass<String>,
    exchange: HttpServerExchange,
  ) {
    val charset = headers.charset?.let { Charsets.get(it) } ?: Charsets.UTF8
    exchange.output.bufferedWriter(charset = charset).use {
      it.append(value)
    }
  }

  override suspend fun decodePath(
    key: String,
    value: String,
    type: KClass<String>,
    exchange: HttpServerExchange,
  ): String {
    TODO("Not yet implemented")
  }
}
