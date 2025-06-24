package pw.binom.jsonrpc.exceptions

import kotlinx.serialization.json.JsonElement
import pw.binom.jsonrpc.JsonRpcErrorCodes

class ParseException(
  override val message: String,
  override val data: JsonElement? = null,
) : JsonRpcException() {
  override val code: Int
    get() = JsonRpcErrorCodes.PARSE_ERROR
}
