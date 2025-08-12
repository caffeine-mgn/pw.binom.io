package pw.binom.http.rest.serialization


import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import pw.binom.http.client.HttpClientCommonExchange
import pw.binom.http.client.HttpRequestBuilder
import pw.binom.io.http.HttpContentLength
import pw.binom.io.http.httpContentLength

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> HttpRequestBuilder.sendJson(
  value: T,
  json: Json = Json,
) = sendJson(
  serializer = T::class.serializer(),
  value = value,
  json = json,
)

suspend fun <T> HttpRequestBuilder.sendJson(
  serializer: KSerializer<T>,
  value: T,
  contentType: String = "application/json",
  json: Json = Json,
): HttpClientCommonExchange {
  headers.contentType = contentType
  headers.httpContentLength = HttpContentLength.CHUNKED
  val req = connect()
  try {
    req.sendJson(
      serializer = serializer,
      value = value,
      json = json,
    )
  } catch (e: Throwable) {
    try {
      req.asyncClose()
    } catch (ex: Throwable) {
      ex.addSuppressed(e)
      throw ex
    }
    throw e
  }
  return req
}

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> HttpClientCommonExchange.readJson(json: Json = Json) =
  readJson(
    serializer = T::class.serializer(),
    json = json
  )

suspend fun <T> HttpClientCommonExchange.readJson(serializer: KSerializer<T>, json: Json = Json): T {
  val responseText = readAllText()
  try {
    return json.decodeFromString(serializer, responseText)
  } catch (e:SerializationException){
    throw SerializationException("Can't decode $responseText to ${serializer.descriptor.serialName}", e)
  }
}

suspend fun <T> HttpClientCommonExchange.sendJson(serializer: KSerializer<T>, value: T, json: Json = Json) {
  val requestText = json.encodeToString(serializer, value)
  sendText(requestText)
}
