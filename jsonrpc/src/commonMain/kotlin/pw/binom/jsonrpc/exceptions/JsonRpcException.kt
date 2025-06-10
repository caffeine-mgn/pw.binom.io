package pw.binom.jsonrpc.exceptions

open class JsonRpcException : Exception {
  constructor() : super()
  constructor(message: String?) : super(message)
  constructor(cause: Throwable?) : super(cause)
  constructor(message: String?, cause: Throwable?) : super(message, cause)
}
