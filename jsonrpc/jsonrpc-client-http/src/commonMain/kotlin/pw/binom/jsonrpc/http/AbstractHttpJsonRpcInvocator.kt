package pw.binom.jsonrpc.http

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import pw.binom.http.client.HttpClientCommon
import pw.binom.http.client.HttpClientCommonExchange
import pw.binom.io.http.Headers
import pw.binom.io.http.headersOf
import pw.binom.io.useAsync
import pw.binom.jsonrpc.JsonRpcInvocator
import pw.binom.jsonrpc.JsonRpcRequest
import pw.binom.jsonrpc.JsonRpcResponse
import pw.binom.url.URL

abstract class AbstractHttpJsonRpcInvocator : JsonRpcInvocator {
  protected abstract val httpClient: HttpClientCommon

  protected abstract val baseUrl: URL

  protected open fun preRequest(request: HttpClientCommonExchange) = request

  protected open fun buildRequest() = httpClient.request(
    method = "POST",
    url = baseUrl,
    headers = headersOf(Headers.CONTENT_TYPE to "application/json")
  )

  protected open suspend fun postRequest(request: HttpClientCommonExchange) {
    val responseCode = request.getResponseCode()
    check(responseCode == 200) { "Invalid response code: $responseCode" }
    val responseHeaders = request.getResponseHeaders()
    val contentType = responseHeaders.contentType
    check(contentType == null || contentType.startsWith("application/json")) { "Invalid response content-type: $contentType" }
  }

  override suspend fun invoke(request: JsonRpcRequest): JsonRpcResponse {
    val requestJson = Json.encodeToString(JsonElement.serializer(), request.json)
    val responseJson = preRequest(buildRequest().connect()).useAsync { connection ->
      connection.sendText(requestJson)
      postRequest(connection)
      connection.readAllText()
    }
    return JsonRpcResponse.parse(Json.parseToJsonElement(responseJson))
  }
}
