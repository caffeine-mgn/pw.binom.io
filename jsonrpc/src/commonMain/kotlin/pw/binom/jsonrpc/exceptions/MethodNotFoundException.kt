package pw.binom.jsonrpc.exceptions

import kotlinx.serialization.json.JsonElement
import pw.binom.jsonrpc.JsonRpcErrorCodes

class MethodNotFoundException(
  override val message: String,
  override val data: JsonElement? = null,
) : JsonRpcException() {
  override val code: Int
    get() = JsonRpcErrorCodes.METHOD_NOT_FOUND
}
