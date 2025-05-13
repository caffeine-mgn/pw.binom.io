package pw.binom.http.rest

import pw.binom.io.httpServer.HttpHandler
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.url.PathMask

abstract class RouteHttpHandler : HttpHandler {
  private class Caller

  interface Context {
    fun pathParam(name: String): String?
  }

  private val methods = HashMap<String, PathTree<Caller>>()
  protected fun endpoint(method: String, path: PathMask, func: (Context) -> Unit) {
    val tree = methods.getOrPut(method) { PathTree { Caller() } }
    val caller = tree.getMask(path)
  }

  override suspend fun handle(exchange: HttpServerExchange) {
    TODO("Not yet implemented")
  }
}
