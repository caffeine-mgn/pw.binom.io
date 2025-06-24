package pw.binom.jsonrpc.exceptions

import kotlinx.serialization.json.JsonElement
import pw.binom.jsonrpc.JsonRpcResponse

abstract class JsonRpcException : Exception {
  abstract val code: Int

  abstract override val message: String
  abstract val data: JsonElement?

  constructor() : super()
  constructor(message: String?) : super(message)
  constructor(cause: Throwable?) : super(cause)
  constructor(message: String?, cause: Throwable?) : super(message, cause)

  fun asResponseContent() = JsonRpcResponse.Content.Error(
    message = message,
    code = code,
    data = data,
  )
}
