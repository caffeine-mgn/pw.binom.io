package pw.binom.jsonrpc.exceptions

import kotlinx.serialization.json.JsonElement

class BaseJsonRpcException(
  override val code: Int,
  override val message: String,
  override val data: JsonElement?,
) : JsonRpcException()
