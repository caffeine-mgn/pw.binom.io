package pw.binom.io.httpServer.encoders

import pw.binom.charset.Charsets
import pw.binom.io.bufferedReader
import pw.binom.io.bufferedWriter
import pw.binom.io.http.MutableHeaders
import pw.binom.io.httpServer.HttpEncoder
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.io.use
import kotlin.reflect.KClass

object IntHttpEncoder : HttpEncoder<Int> {
  override suspend fun decodeQuery(key: String, value: String?, type: KClass<Int>, exchange: HttpServerExchange): Int {
    value ?: throw IllegalArgumentException("Query argument \"$key\" is null")
    return value.toIntOrNull() ?: throw IllegalArgumentException("Can't parse \"$value\" to int")
  }

  override suspend fun decodeRequestBody(exchange: HttpServerExchange, type: KClass<Int>): Int {
    val charset = exchange.requestHeaders.charset?.let { Charsets.get(it) } ?: Charsets.UTF8
    val value = exchange.input.bufferedReader(charset = charset).use {
      it.readText()
    }
    return value.toIntOrNull() ?: throw IllegalArgumentException("Can't parse \"$value\" to int")
  }

  override suspend fun encodeResponseBody(
    value: Int,
    headers: MutableHeaders,
    type: KClass<Int>,
    exchange: HttpServerExchange,
  ) {
    val charset = headers.charset?.let { Charsets.get(it) } ?: Charsets.UTF8
    exchange.output.bufferedWriter(charset = charset).use {
      it.append(value)
    }
  }

  override suspend fun decodePath(key: String, value: String, type: KClass<Int>, exchange: HttpServerExchange): Int {
    TODO("Not yet implemented")
  }
}
