package pw.binom.http.rest

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pw.binom.date.DateTime
import pw.binom.date.rfc822
import pw.binom.http.rest.endpoints.Endpoint2
import pw.binom.http.rest.serialization.HttpInputDecoder
import pw.binom.http.rest.serialization.HttpOutputEncoder
import pw.binom.io.http.Headers
import pw.binom.io.http.forEachHeader
import pw.binom.io.httpServer.HttpHandler
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.url.PathMask
import pw.binom.url.UrlEncoder
import kotlin.time.Duration

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
    val func: suspend HttpRequestScope.(REQUEST) -> RESPONSE,
  ) : HttpHandler {
    private inner class HttpRequestScopeImpl : HttpRequestScope {
      var setCookie: ((String) -> Unit)? = null
      var getCookie: ((String) -> String?)? = null
      var getHeader: ((String) -> List<String>)? = null
      var addHeader: ((String, String) -> Unit)? = null

      override fun setResponseCookie(
        key: String,
        value: String,
        expires: DateTime?,
        maxAge: Duration?,
        domain: String?,
        path: String?,
        secure: Boolean,
        httpOnly: Boolean,
        sameSite: HttpRequestScope.SameSite?,
      ) {
        val setCookie = setCookie ?: return
        val sb =
          StringBuilder(UrlEncoder.encode(key))
            .append("=")
            .append(UrlEncoder.encode(value))
        if (expires != null) {
          sb.append("; Expires=").append(expires.rfc822())
        }
        if (maxAge != null) {
          sb.append("; Max-Age=").append(maxAge.inWholeSeconds)
        }
        if (domain != null) {
          sb.append("; Domain=").append(UrlEncoder.encode(domain))
        }
        if (path != null) {
          sb.append("; Path=").append(UrlEncoder.pathEncode(path))
        }
        if (secure) {
          sb.append("; Secure")
        }
        if (httpOnly) {
          sb.append("; HttpOnly")
        }
        if (sameSite != null) {
          val sameSiteValue =
            when (sameSite) {
              HttpRequestScope.SameSite.LAX -> "Lax"
              HttpRequestScope.SameSite.STRICT -> "Strict"
              HttpRequestScope.SameSite.NONE -> "None"
            }
          sb.append("; SameSite=").append(sameSiteValue)
        }
        setCookie(sb.toString())
      }

      override fun getRequestCookie(name: String): String? = getCookie?.invoke(name)
      override fun getRequestHeader(name: String): List<String> = getHeader?.invoke(name) ?: emptyList()
      override fun addResponseHeader(headers: Headers) {
        headers.forEachHeader { key, value ->
          addHeader?.invoke(key, value)
        }
      }
    }

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
      val resp = exchange.response()
      val ctx = HttpRequestScopeImpl()
      ctx.setCookie = { resp.headers.add(Headers.SET_COOKIE, it) }
      val cookies by lazy { exchange.requestHeaders.getCookies() }
      ctx.getCookie = { cookies[it] }
      ctx.getHeader = { exchange.requestHeaders.get(it) ?: emptyList() }
      ctx.addHeader = { key, value -> resp.headers.add(key, value) }
      val response = func(ctx, requestDescription.serializer.deserialize(d))
      val e = HttpOutputEncoder(serializersModule, responseDescription as EndpointDescription<Any?>)
      responseDescription.serializer.serialize(e, response)

      val respBody = e.body

      resp.headers.add(e.headerParams)

      resp.status =
        if (e.responseCode > 0) {
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
    func: suspend HttpRequestScope.(REQUEST) -> RESPONSE,
  ) {
    val inputDesc = EndpointDescription.create(endpoint.request)
    val outputDesc = EndpointDescription.create(endpoint.response)

//    if (inputDesc.bodyDescription != null && endpoint.request !is BodyContext<*>) {
//      throw IllegalArgumentException("Invalid input ${endpoint.request.descriptor.serialName}: with @BodyParam you should implement BodyContext")
//    }
//    if (outputDesc.bodyDescription != null && endpoint.response !is BodyContext<*>) {
//      throw IllegalArgumentException("Invalid output ${endpoint.response.descriptor.serialName}: with @BodyParam you should implement BodyContext")
//    }
    handlers +=
      EndpointHandler(
        path = endpoint.path,
        method = endpoint.method,
        requestDescription = inputDesc,
        responseDescription = outputDesc,
        func = func,
      )
  }
}
