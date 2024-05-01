package pw.binom.http.rest

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.http.rest.endpoints.Endpoint2
import pw.binom.http.rest.serialization.HttpInputDecoder
import pw.binom.http.rest.serialization.HttpOutputEncoder
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.HttpRequest
import pw.binom.io.httpClient.HttpResponse
import pw.binom.io.useAsync
import pw.binom.url.PathMask
import pw.binom.url.URL

abstract class RestHttpClient {
  protected abstract val client: HttpClient
  protected abstract val baseUrl: URL

  protected open val serializersModule: SerializersModule
    get() = EmptySerializersModule()

  protected open suspend fun <TYPE, DATA> getBodyEncode(
    descriptor: SerialDescriptor,
    request: HttpRequest,
  ): EncodeFunc<TYPE, DATA> = EncodeFunc.notSupported()

  protected open suspend fun <TYPE, DATA> getBodyDecoder(
    descriptor: SerialDescriptor?,
    response: HttpResponse,
  ): DecodeFunc<TYPE, DATA> = DecodeFunc.notSupported()

  private val endpointCache = HashMap<KSerializer<*>, EndpointDescription<*>>()
  private val endpointCacheLock = SpinLock()

  @Suppress("UNCHECKED_CAST")
  private fun <T> getDescription(serializer: KSerializer<T>): EndpointDescription<T> =
    endpointCacheLock.synchronize {
      endpointCache.getOrPut(serializer) { EndpointDescription.create(serializer) } as EndpointDescription<T>
    }

  interface RemoteEndpoint<REQUEST : Any, RESPONSE : Any> {
    suspend operator fun invoke(request: REQUEST): RESPONSE
  }

  inner class EndpointImpl<REQUEST : Any, RESPONSE : Any>(val endpoint: Endpoint2<REQUEST, RESPONSE>) :
    RemoteEndpoint<REQUEST, RESPONSE> {
    override suspend operator fun invoke(request: REQUEST): RESPONSE = execute(endpoint, request)
  }

  open fun <REQUEST : Any, RESPONSE : Any> create(endpoint: Endpoint2<REQUEST, RESPONSE>): RemoteEndpoint<REQUEST, RESPONSE> =
    EndpointImpl(endpoint)

  open suspend fun <REQUEST : Any, RESPONSE : Any> execute(
    endpoint2: Endpoint2<REQUEST, RESPONSE>,
    request: REQUEST,
  ): RESPONSE =
    call(
      method = endpoint2.method,
      prefix = endpoint2.path,
      requestDescription = getDescription(endpoint2.request),
      responseDescription = getDescription(endpoint2.response),
      value = request,
    )

  protected open fun preBodyRead(resp: HttpResponse) {
    // Do nothing
  }

  protected open suspend fun <RESPONSE> decodeResponse(
    resp: HttpResponse,
    responseDescription: EndpointDescription<RESPONSE>,
    prefix: PathMask,
  ): RESPONSE {
    val httpDecoder = HttpInputDecoder()
    httpDecoder.serializersModule = serializersModule
    if (responseDescription.bodyIndex != -1) {
      val decoder =
        getBodyDecoder<RESPONSE, Any?>(
          descriptor = responseDescription.bodyDescription,
          response = resp,
        )
      httpDecoder.reset(
        input = resp,
        description = responseDescription,
        data = decoder.read(resp),
        body = decoder,
        path = prefix,
        responseCode = resp.responseCode,
      )
    } else {
      httpDecoder.reset<RESPONSE, Any?>(
        input = resp,
        description = responseDescription,
        data = null,
        body = DecodeFunc.notSupported(),
        path = prefix,
        responseCode = resp.responseCode,
      )
    }
    return responseDescription.serializer.deserialize(httpDecoder)
  }

  @Suppress("UNCHECKED_CAST")
  protected open suspend fun <REQUEST, RESPONSE> call(
    method: String,
    prefix: PathMask,
    requestDescription: EndpointDescription<REQUEST>,
    responseDescription: EndpointDescription<RESPONSE>,
    value: REQUEST,
  ): RESPONSE {
    val encoder =
      HttpOutputEncoder(
        serializersModule = serializersModule,
        endpointDescription = requestDescription as EndpointDescription<Any?>,
      )
    requestDescription.serializer.serialize(encoder, value)
    val url = baseUrl.addPath(prefix.toPath(encoder.pathParams)).appendQuery(encoder.getParams)
    return client.connect(
      method = method,
      uri = url,
    ).useAsync { request ->
      request.headers.add(encoder.headerParams)
      val bodyDesc = encoder.body
      if (bodyDesc != null) {
        val bodyEncoder = getBodyEncode<Any, Any?>(descriptor = requestDescription.bodyDescription!!, request = request)
        val data = bodyEncoder.encode(bodyDesc.serializer, bodyDesc.body, request)
        bodyEncoder.send(data, request)
      }
      request.getResponse().useAsync { resp ->
        preBodyRead(resp)
        decodeResponse(
          resp = resp,
          responseDescription = responseDescription,
          prefix = prefix,
        )
      }
    }
  }
}
