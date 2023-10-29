package pw.binom.http.rest

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.http.rest.endpoints.BodyContext
import pw.binom.http.rest.endpoints.Endpoint2
import pw.binom.http.rest.serialization.HttpInputDecoder
import pw.binom.http.rest.serialization.HttpOutputEncoder
import pw.binom.io.httpServer.HttpHandler
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.url.PathMask

@Suppress("UNCHECKED_CAST")
abstract class RestHttpHandler : HttpHandler {
  protected open val serializersModule: SerializersModule
    get() = EmptySerializersModule()

  private val handlers = ArrayList<EndpointHandler<out Any, out Any>>()

  protected open suspend fun <TYPE, DATA> encodeBody(
    descriptor: SerialDescriptor,
    exchange: HttpServerExchange,
  ): EncodeFunc<TYPE, DATA> = EncodeFunc.notSupported()

  protected open suspend fun <TYPE, DATA> decodeBody(
    descriptor: SerialDescriptor?,
    exchange: HttpServerExchange,
  ): DecodeFunc<TYPE, DATA> = DecodeFunc.notSupported()

  override suspend fun handle(exchange: HttpServerExchange) {
    handlers.forEach { handler ->
      if (exchange.requestMethod != handler.method || !exchange.path.isMatch(handler.path)) {
        return@forEach
      }
      handler.handle(exchange)
      if (exchange.responseStarted) {
        return
      }
    }
  }

  inner class EndpointHandler<REQUEST : Any, RESPONSE : Any>(
    val path: PathMask,
    val method: String,
    val requestDescription: EndpointDescription<REQUEST>,
    val responseDescription: EndpointDescription<RESPONSE>,
    val func: suspend (REQUEST) -> RESPONSE,
  ) : HttpHandler {
    override suspend fun handle(exchange: HttpServerExchange) {
      val d = HttpInputDecoder()
      d.serializersModule = serializersModule
      if (requestDescription.bodyIndex != -1) {
        val decoder = decodeBody<REQUEST, Any?>(requestDescription.bodyDescription, exchange)
        val inputData = decoder.read(exchange)
        d.reset(
          input = exchange,
          description = requestDescription,
          data = inputData,
          body = decoder,
          path = path,
          responseCode = 0,
        )
      } else {
        d.reset(
          input = exchange,
          description = requestDescription,
          data = null,
          body = DecodeFunc.notSupported<REQUEST, RESPONSE>(),
          path = path,
          responseCode = 0,
        )
      }

      val response = func(requestDescription.serializer.deserialize(d))
      val e = HttpOutputEncoder(serializersModule, responseDescription as EndpointDescription<Any?>)
      responseDescription.serializer.serialize(e, response)

      val respBody = e.body
      val resp = exchange.response()
      resp.headers.add(e.headerParams)
      resp.status = if (e.responseCode > 0) {
        e.responseCode
      } else {
        200
      }
      if (respBody != null) {
        val encoder = encodeBody<Any, Any>(descriptor = respBody.serializer.descriptor, exchange = exchange)
        val outputData = encoder.encode(serializer = respBody.serializer, value = respBody.body, output = resp)
        encoder.send(outputData, resp)
      }
      if (!exchange.responseStarted) {
        resp.startOutput().asyncCloseAnyway()
      }
    }
  }

  protected fun <REQUEST : Any, RESPONSE : Any> endpoint(
    endpoint: Endpoint2<REQUEST, RESPONSE>,
    func: suspend (REQUEST) -> RESPONSE,
  ) {
    val inputDesc = EndpointDescription.create(endpoint.request)
    val outputDesc = EndpointDescription.create(endpoint.response)

//    if (inputDesc.bodyDescription != null && endpoint.request !is BodyContext<*>) {
//      throw IllegalArgumentException("Invalid input ${endpoint.request.descriptor.serialName}: with @BodyParam you should implement BodyContext")
//    }
//    if (outputDesc.bodyDescription != null && endpoint.response !is BodyContext<*>) {
//      throw IllegalArgumentException("Invalid output ${endpoint.response.descriptor.serialName}: with @BodyParam you should implement BodyContext")
//    }
    handlers += EndpointHandler(
      path = endpoint.path,
      method = endpoint.method,
      requestDescription = inputDesc,
      responseDescription = outputDesc,
      func = func,
    )
  }
}
