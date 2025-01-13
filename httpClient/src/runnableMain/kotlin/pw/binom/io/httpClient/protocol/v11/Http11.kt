package pw.binom.io.httpClient.protocol.v11

import pw.binom.io.AsyncReader
import pw.binom.io.AsyncWriter
import pw.binom.io.EOFException
import pw.binom.io.IOException
import pw.binom.io.http.*
import pw.binom.io.httpClient.protocol.v11.Http11ConnectFactory2.Http1Version
import pw.binom.io.httpClient.protocol.v11.Http11ConnectFactory2.Response
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

object Http11 {

  private suspend fun sendHeader(output: AsyncWriter, method: String, request: String) {
    output.append(method).append(" ").append(request).append(" ").append("HTTP/1.1").append(Utils.CRLF)
  }

  suspend fun sendRequest(output: AsyncWriter, method: String, request: String, headers: Headers) {
    sendHeader(
      output = output,
      method = method,
      request = request,
    )
    headers.forEachHeader { key, value ->
      output.append(key).append(": ").append(value).append(Utils.CRLF)
    }
    output.append(Utils.CRLF)
  }

  suspend fun sendRequest(output: AsyncWriter, method: String, request: String, headers: SimpleHeaders) {
    sendHeader(
      output = output,
      method = method,
      request = request,
    )
    headers.forEach { key, value ->
      output.append(key).append(": ").append(value).append(Utils.CRLF)
    }
    output.append(Utils.CRLF)
  }

  @OptIn(ExperimentalContracts::class)
  suspend inline fun readResponse(
    input: AsyncReader,
    httpVersion: (String) -> Unit = {},
    responseCode: (Int) -> Unit = {},
    header: (String, String) -> Unit = { _, _ -> },
  ) {
    contract {
      callsInPlace(httpVersion, InvocationKind.EXACTLY_ONCE)
      callsInPlace(responseCode, InvocationKind.EXACTLY_ONCE)
      callsInPlace(header, InvocationKind.UNKNOWN)
    }
    val title = input.readln() ?: throw EOFException()
    val titleEndIndex = title.indexOf(' ')
    if (titleEndIndex == -1) {
      throw IOException("HTTP version not found")
    }
    httpVersion(title.substring(0, titleEndIndex))

    val responseCodeText = title.substring(titleEndIndex + 1, 12)
    responseCode(responseCodeText.toInt())
    while (true) {
      val str = input.readln() ?: throw EOFException()
      if (str.isEmpty()) {
        break
      }
      val items = str.split(": ", limit = 2)
      header(items[0], items.getOrNull(1) ?: "")
    }
  }
}
