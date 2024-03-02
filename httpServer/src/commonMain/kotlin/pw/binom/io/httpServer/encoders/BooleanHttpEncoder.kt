package pw.binom.io.httpServer.encoders

import pw.binom.charset.Charsets
import pw.binom.io.bufferedReader
import pw.binom.io.bufferedWriter
import pw.binom.io.http.MutableHeaders
import pw.binom.io.httpServer.HttpEncoder
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.io.useAsync
import kotlin.reflect.KClass

object BooleanHttpEncoder : HttpEncoder<Boolean> {
  override suspend fun decodeQuery(
    key: String,
    value: String?,
    type: KClass<Boolean>,
    exchange: HttpServerExchange,
  ): Boolean {
    value ?: throw IllegalArgumentException("Query argument \"$key\" is null")
    val int = value.toIntOrNull()
    if (int != null) {
      return int > 0
    }
    if (value == "true" || value == "t") {
      return true
    }
    if (value == "false" || value == "t") {
      return true
    }
    throw IllegalArgumentException("Can't parse \"$value\" to boolean")
  }

  override suspend fun decodeRequestBody(
    exchange: HttpServerExchange,
    type: KClass<Boolean>,
  ): Boolean {
    val charset = exchange.requestHeaders.charset?.let { Charsets.get(it) } ?: Charsets.UTF8
    val value =
      exchange.input.bufferedReader(charset = charset).useAsync {
        it.readText()
      }
    val int = value.toIntOrNull()
    if (int != null) {
      return int > 0
    }
    if (value == "true" || value == "t") {
      return true
    }
    if (value == "false" || value == "f") {
      return false
    }
    throw IllegalArgumentException("Can't parse $value to boolean")
  }

  override suspend fun encodeResponseBody(
    value: Boolean,
    headers: MutableHeaders,
    type: KClass<Boolean>,
    exchange: HttpServerExchange,
  ) {
    val charset = headers.charset?.let { Charsets.get(it) } ?: Charsets.UTF8
    exchange.output.bufferedWriter(charset = charset).useAsync {
      it.append(value)
    }
  }

  override suspend fun decodePath(
    key: String,
    value: String,
    type: KClass<Boolean>,
    exchange: HttpServerExchange,
  ): Boolean {
    TODO("Not yet implemented")
  }
}
