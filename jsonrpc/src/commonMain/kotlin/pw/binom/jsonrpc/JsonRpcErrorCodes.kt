package pw.binom.jsonrpc

object JsonRpcErrorCodes {
  /**
   * Invalid JSON was received by the server.
   * An error occurred on the server while parsing the JSON text.
   */
  const val PARSE_ERROR = -32700

  /**
   * The JSON sent is not a valid Request object.
   */
  const val INVALID_REQUEST = -32600

  /**
   * The method does not exist / is not available.
   */
  const val METHOD_NOT_FOUND = -32601

  /**
   * Invalid method parameter(s).
   */
  const val INVALID_PARAMS = -32602

  /**
   * Internal JSON-RPC error.
   */
  const val INTERNAL_ERROR = -32603
}
