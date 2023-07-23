package pw.binom.io.httpServer

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

class HttpServerScope(
  override val coroutineContext: CoroutineContext,
  val server: HttpServer2,
) : CoroutineScope
