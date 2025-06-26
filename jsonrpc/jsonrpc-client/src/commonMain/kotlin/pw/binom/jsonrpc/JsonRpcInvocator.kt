package pw.binom.jsonrpc

interface JsonRpcInvocator {
  suspend fun invoke(request: JsonRpcRequest): JsonRpcResponse
}
