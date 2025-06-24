package pw.binom.jsonrpc.exceptions

import kotlinx.serialization.json.JsonElement
import pw.binom.jsonrpc.JsonRpcErrorCodes

class InvalidRequestException(
  override val message: String,
  override val data: JsonElement? = null,
) : JsonRpcException() {
  override val code: Int
    get() = JsonRpcErrorCodes.INVALID_REQUEST
}
