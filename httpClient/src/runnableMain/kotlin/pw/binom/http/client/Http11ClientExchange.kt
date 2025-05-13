package pw.binom.http.client

import pw.binom.SafeException
import pw.binom.http.client.io.AsyncInputNoClose
import pw.binom.http.client.io.AsyncOutputNoClose
import pw.binom.io.*
import pw.binom.io.http.*
import pw.binom.io.httpClient.protocol.v11.Http11

class Http11ClientExchange(
  private val input: AsyncBufferedAsciiInputReader,
  private val output: AsyncOutput,
  private val rawOutput: AsyncOutput,
  private val onClose: suspend (Boolean) -> Unit,
) : HttpClientExchange {
  private var state = State.SENDING_REQUEST

  private enum class State {
    SENDING_REQUEST,
    RECEIVING_RESPONSE,
    CLOSED,
  }

  fun toRaw(): Pair<AsyncInput, AsyncOutput> {
    state = State.CLOSED
    return input to rawOutput
  }

//  private var outputWrapper = AsyncOutputNoClose(output)
  private var inputWrapper: AsyncInputNoClose? = null// = AsyncInputNoClose(input)

  override fun getOutput(): AsyncOutput = when (state) {
    State.RECEIVING_RESPONSE -> throw IllegalStateException("Request already sent")
    State.CLOSED -> throw IllegalStateException("Request already closed")
    State.SENDING_REQUEST -> output
  }

  override suspend fun getInput(): AsyncInput = when (state) {
    State.RECEIVING_RESPONSE -> inputWrapper!!
    State.CLOSED -> throw IllegalStateException("Request already closed")
    State.SENDING_REQUEST -> {
      makeRequest()
      inputWrapper!!
    }
  }

  private suspend fun makeRequest() {
    when (state) {
      State.SENDING_REQUEST -> {
        state = State.RECEIVING_RESPONSE
        val respHeaders = HashHeaders2()
        SafeException.async {
          onException {
            state = State.CLOSED
            onClose(true)
          }
          Http11.readResponse(
            input = input,
            httpVersion = {
            },
            responseCode = {
              responseCode = it
            },
            header = { key, value ->
              respHeaders.add(key = key, value = value)
            }
          )
        }
        responseHeaders = respHeaders
        val contentLength = respHeaders.contentLength
        val transferEncoding = respHeaders.transferEncoding
        val input = when {
          contentLength != null && transferEncoding?.equals(
            Encoding.CHUNKED,
            ignoreCase = true
          ) == true -> {
            AsyncContentLengthInput(
              stream = AsyncChunkedInput(
                stream = input,
                closeStream = false,
              ),
              contentLength = contentLength,
              closeStream = false
            )
          }

          transferEncoding?.equals(
            Encoding.CHUNKED,
            ignoreCase = true
          ) == true -> AsyncChunkedInput(
            stream = input,
            closeStream = false,
          )

          contentLength != null ->
            AsyncContentLengthInput(
              stream = input,
              contentLength = contentLength,
              closeStream = false
            )

          else -> AsyncEmptyHttpInput
        }
        inputWrapper = AsyncInputNoClose(input)
      }

      State.RECEIVING_RESPONSE -> {
        // do nothing
      }

      State.CLOSED -> throw IllegalStateException("Request already closed")
    }
  }

  private var responseHeaders: Headers? = null
  private var responseCode = 0
  override suspend fun getResponseHeaders(): Headers {
    makeRequest()
    return responseHeaders ?: throw IllegalStateException("Header missing")
  }

  override suspend fun getResponseCode(): Int {
    makeRequest()
    return responseCode
  }

  override val isEof: Boolean
    get() {
      if (state == State.CLOSED) {
        return true
      }
      val input = inputWrapper ?: return false
      return input.isEof
    }

  override suspend fun asyncClose() {
    if (state == State.CLOSED) {
      return
    }
    state = State.CLOSED
    val inputWrapper = inputWrapper
    if (inputWrapper != null) {
      if (inputWrapper.isEof) {
        if (responseHeaders?.keepAlive == true) {
          onClose(false)
        } else {
          onClose(true)
        }
      } else {
        onClose(true)
      }
    } else {
      onClose(true)
    }
  }
}
