package pw.binom.jsonrpc.server.http

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import pw.binom.io.bufferedReader
import pw.binom.io.httpServer.HttpHandler
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.io.httpServer.response
import pw.binom.io.useAsync
import pw.binom.jsonrpc.JsonRpcRequest
import pw.binom.jsonrpc.JsonRpcResponse

abstract class JsonRpcHandler : HttpHandler {
  protected abstract suspend fun execute(request: JsonRpcRequest.Single): JsonRpcResponse.Single?
  override suspend fun handle(exchange: HttpServerExchange) {
    val contentType = exchange.requestHeaders.contentType
    if (contentType == null || contentType.startsWith("application/json")) {
      val requestJson = exchange.input.bufferedReader().useAsync { it.readText() }
      val request = Json.parseToJsonElement(requestJson)
      val response = JsonRpcRequest.execute(request) { req -> execute(req) }?.json
      if (response != null) {
        exchange.response {
          status = 200
          headers.contentType = "application/json"
          send(Json.encodeToString(JsonElement.serializer(), response))
        }
      } else {
        exchange.response {
          status = 200
        }
      }
    }
  }
}
