package pw.binom.http.rest

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.http.rest.serialization.HttpInputDecoder
import pw.binom.http.rest.serialization.HttpOutputEncoder
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.HttpRequest
import pw.binom.io.httpClient.HttpResponse
import pw.binom.io.use
import pw.binom.url.PathMask
import pw.binom.url.URL

abstract class RestHttpClient {
  abstract val client: HttpClient
  abstract val baseUrl: URL

  protected open val serializersModule: SerializersModule
    get() = EmptySerializersModule()

  abstract suspend fun <TYPE, DATA> getBodyEncode(
    descriptor: SerialDescriptor,
    request: HttpRequest,
  ): EncodeFunc<TYPE, DATA>

  abstract suspend fun <TYPE, DATA> getBodyDecoder(
    descriptor: SerialDescriptor?,
    response: HttpResponse,
  ): DecodeFunc<TYPE, DATA>

  @Suppress("UNCHECKED_CAST")
  private suspend fun <REQUEST, RESPONSE> call(
    method: String,
    prefix: PathMask,
    requestDescription: EndpointDescription<REQUEST>,
    responseDescription: EndpointDescription<RESPONSE>,
    value: REQUEST,
  ): RESPONSE {
    val encoder = HttpOutputEncoder(
      serializersModule = serializersModule,
      endpointDescription = requestDescription as EndpointDescription<Any?>,
    )
    val url = baseUrl.addPath(prefix.toPath(encoder.pathParams)).appendQuery(encoder.getParams)
    requestDescription.serializer.serialize(encoder, value)
    return client.connect(
      method = method,
      uri = url,
    ).use { request ->
      request.headers.add(encoder.headerParams)
      if (requestDescription.bodyIndex != -1) {
        val encoder = getBodyEncode<REQUEST, Any?>(descriptor = requestDescription.bodyDescription!!, request = request)
        val data = encoder.encode(requestDescription.serializer, value, request)
        encoder.send(data, request)
      }
      request.getResponse().use { resp ->
        val httpDecoder = HttpInputDecoder()
        httpDecoder.serializersModule = serializersModule
        if (responseDescription.bodyIndex != -1) {
          val decoder = getBodyDecoder<RESPONSE, Any?>(
            descriptor = responseDescription.bodyDescription,
            response = resp,
          )
          httpDecoder.reset(
            input = resp,
            description = responseDescription,
            data = decoder.read(resp),
            body = decoder,
          )
        } else {
          httpDecoder.reset<RESPONSE, Any?>(
            input = resp,
            description = responseDescription,
            data = null,
            body = DecodeFunc.notSupported(),
          )
        }
        responseDescription.serializer.deserialize(httpDecoder)
      }
    }
  }
}
