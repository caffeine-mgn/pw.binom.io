package pw.binom.flux

import kotlinx.serialization.KSerializer
import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncInput
import pw.binom.io.AsyncReader
import pw.binom.io.http.Headers
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.url.Path
import pw.binom.url.Query
import kotlin.coroutines.AbstractCoroutineContextElement

@Deprecated(message = "Use HttpServer2")
class FluxHttpRequest2Impl(val original: HttpRequest, val mask: String, val serialization: FluxServerSerialization) :
  FluxHttpRequest, HttpRequest,
  AbstractCoroutineContextElement(FluxHttpRequestImplKey) {
  private var internalPathVariables: Map<String, String>? = null
  private var internalQueryVariables: Map<String, List<String?>>? = null
//    private var serialization: FluxServerSerialization = FluxServerSerializationStab

  override val pathVariables: Map<String, String>
    get() {
      val internalPathVariables = internalPathVariables
      if (internalPathVariables == null) {
        val p = path.getVariables(mask = mask)!!
        this.internalPathVariables = p
        return p
      }
      return internalPathVariables
    }
  override val queryVariables: Map<String, List<String?>>
    get() {
      val internalQueryVariables = internalQueryVariables
      if (internalQueryVariables == null) {
        val p = query?.toMap() ?: emptyMap()
        this.internalQueryVariables = p
        return p
      }
      return internalQueryVariables
    }

  override suspend fun <T : Any> readRequest(serializer: KSerializer<T>): T =
    serialization.decode(
      request = this,
      serializer = serializer,
    )

  override suspend fun <T : Any> finishResponse(
    serializer: KSerializer<T>,
    value: T,
    headers: Headers,
    statusCode: Int?,
  ) {
    try {
      serialization.encode(
        request = this,
        value = value,
        serializer = serializer,
        headers = headers,
        statusCode = statusCode,
      )
    } finally {
      response?.asyncClose()
    }
  }

  override val method: String
    get() = original.method
  override val headers: Headers
    get() = original.headers
  override val path: Path
    get() = original.path
  override val query: Query?
    get() = original.query
  override val request: String
    get() = original.request

  override fun readBinary(): AsyncInput = original.readBinary()

  override fun readText(): AsyncReader = original.readText()

  override suspend fun acceptWebsocket(masking: Boolean): WebSocketConnection = original.acceptWebsocket(masking)

  override suspend fun sendReject() {
    original.sendReject()
  }

  override suspend fun acceptTcp(): AsyncChannel = original.acceptTcp()

  override suspend fun response(): HttpResponse = original.response()

  override val response: HttpResponse?
    get() = original.response
  override val isReadyForResponse: Boolean
    get() = original.isReadyForResponse

  override suspend fun asyncClose() {
    original.asyncClose()
  }
}
