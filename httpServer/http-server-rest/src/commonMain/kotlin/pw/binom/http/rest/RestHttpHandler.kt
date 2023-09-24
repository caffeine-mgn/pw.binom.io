package pw.binom.http.rest

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.http.rest.endpoints.BodyContext
import pw.binom.http.rest.endpoints.Endpoint2
import pw.binom.http.rest.serialization.HttpInputDecoder
import pw.binom.http.rest.serialization.HttpOutputEncoder
import pw.binom.io.httpServer.HttpHandler
import pw.binom.io.httpServer.HttpServerExchange

@Suppress("UNCHECKED_CAST")
abstract class RestHttpHandler : HttpHandler {
  open protected val serializersModule: SerializersModule
    get() = EmptySerializersModule()

  abstract suspend fun <TYPE, DATA> encodeBody(
    descriptor: SerialDescriptor,
    exchange: HttpServerExchange,
  ): EncodeFunc<TYPE, DATA>

  abstract suspend fun <TYPE, DATA> decodeBody(
    descriptor: SerialDescriptor?,
    exchange: HttpServerExchange,
  ): DecodeFunc<TYPE, DATA>

  inner class EndpointHandler<REQUEST,RESPONSE>(
    val requestDescription: EndpointDescription<REQUEST>,
    val responseDescription: EndpointDescription<RESPONSE>,
    val func: (REQUEST) -> RESPONSE,
  ) : HttpHandler {
    override suspend fun handle(exchange: HttpServerExchange) {
      val d = HttpInputDecoder()
      d.serializersModule = serializersModule
      if (requestDescription.bodyIndex!=-1) {
        val decoder = decodeBody<REQUEST, Any?>(requestDescription.bodyDescription, exchange)
        val inputData = decoder.read(exchange)
        d.reset(exchange, requestDescription, inputData, decoder)
      } else {
        d.reset(exchange, requestDescription, null, DecodeFunc.notSupported<REQUEST,RESPONSE>())
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

  protected fun <INPUT : Any, OUTPUT : Any> endpoint(endpoint: Endpoint2<INPUT, OUTPUT>, func: (INPUT) -> OUTPUT) {
    val inputDesc = EndpointDescription.create(endpoint.request as KSerializer<Any>)
    val outputDesc = EndpointDescription.create(endpoint.response as KSerializer<Any>)

    if (inputDesc.bodyDescription != null && endpoint.request !is BodyContext<*>) {
      throw IllegalArgumentException("Invalid input ${endpoint.request.descriptor.serialName}: with @BodyParam you should implement BodyContext")
    }
    if (outputDesc.bodyDescription != null && endpoint.response !is BodyContext<*>) {
      throw IllegalArgumentException("Invalid output ${endpoint.response.descriptor.serialName}: with @BodyParam you should implement BodyContext")
    }
  }
}
