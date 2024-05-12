package pw.binom.strong.web.server

import kotlinx.coroutines.Job
import pw.binom.io.ByteBufferFactory
import pw.binom.io.httpServer.HttpHandler
import pw.binom.io.httpServer.HttpHandlerChain
import pw.binom.io.httpServer.HttpServer2
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.io.socket.DomainNetworkAddress
import pw.binom.io.socket.DomainSocketAddress
import pw.binom.io.socket.InetSocketAddress
import pw.binom.logger.Logger
import pw.binom.logger.debug
import pw.binom.logger.info
import pw.binom.network.NetworkManager
import pw.binom.pool.GenericObjectPool
import pw.binom.strong.BeanLifeCycle
import pw.binom.strong.inject
import pw.binom.strong.injectServiceList
import pw.binom.strong.map

class WebServerService {
  private val listenJob = ArrayList<Job>()
  private val networkManager by inject<NetworkManager>()
  private val webServerProperties by inject<WebServerProperties>().map { it.server!! }
  private val bufferPool by lazy { GenericObjectPool(ByteBufferFactory(webServerProperties.poolSize)) }
  private var server: HttpServer2? = null
  private val handlers by injectServiceList<HttpHandler>()
  private val chains by injectServiceList<HttpHandlerChain>()
  private val logger = Logger.getLogger("WebServer")

  init {
    BeanLifeCycle.postConstruct {
      val serverLocal =
        HttpServer2(
          handler =
            HttpHandler.chain(
              chains = chains,
              next =
                object : HttpHandler {
                  override suspend fun handle(exchange: HttpServerExchange) {
//                                    try {
                    handlers.forEach {
                      it.handle(exchange)
                      if (exchange.responseStarted) {
                        return
                      }
                    }
                    logger.info("Unknown call ${exchange.requestMethod} ${exchange.requestURI}")
                    exchange.startResponse(404)
//                                    } catch (e: Throwable) {
//                                        logger.warn(text = "Exception on http request processing", exception = e)
//                                        if (!exchange.responseStarted) {
//                                            exchange.startResponse(500)
//                                        }
//                                    }
                  }
                },
            ),
          dispatcher = networkManager,
          readTimeout = webServerProperties.readTimeout,
          byteBufferPool = bufferPool,
          maxRequestLength = webServerProperties.maxRequestLength,
          maxHeaderLength = webServerProperties.maxHeaderLength,
          compressing = webServerProperties.compressing,
          keepAlive = webServerProperties.keepAlive,
          idleCheckInterval = webServerProperties.idleCheckInterval,
        )

      try {
        val bindAddresses = ArrayList<InetSocketAddress>()
        webServerProperties.bindAddresses.forEach {
          val items = it.split(':', limit = 2)
          bindAddresses +=
            DomainSocketAddress(
              host = items[0],
              port = items[1].toInt(),
            ).resolve()
        }
        val singlePort =
          when {
            webServerProperties.port != null -> webServerProperties.port!!
            webServerProperties.port == null && webServerProperties.bindAddresses.isEmpty() -> 8080
            else -> null
          }
        if (singlePort != null) {
          bindAddresses +=
            InetSocketAddress.resolve(
              host = "0.0.0.0",
              port = singlePort,
            )
        }
        bindAddresses.forEach {
          logger.debug("Start listen on $it")
          listenJob += serverLocal.listen(it)
        }
      } catch (e: Throwable) {
        listenJob.forEach {
          it.cancel()
        }
        serverLocal.asyncCloseAnyway()
        throw e
      }
      listenJob.trimToSize()
      server = serverLocal
    }
    BeanLifeCycle.preDestroy {
      listenJob.forEach {
        it.cancel()
      }
      server?.asyncClose()
      bufferPool.closeAnyway()
      logger.info("Goodbye")
    }
  }
}
