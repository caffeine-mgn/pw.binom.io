package pw.binom.io.httpServer

class QueryVariableNotFoundException(val variableName: String) : IllegalArgumentException() {
  override val message: String
    get() = "Query variable \"$variableName\" not found"
}
