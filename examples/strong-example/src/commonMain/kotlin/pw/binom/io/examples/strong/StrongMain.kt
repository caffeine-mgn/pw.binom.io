package pw.binom.io.examples.strong

import pw.binom.flux.RootRouter
import pw.binom.flux.Route
import pw.binom.flux.get
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpServer
import pw.binom.network.NetworkDispatcher
import pw.binom.strong.Strong
import pw.binom.strong.StrongApplication
import pw.binom.strong.bean
import pw.binom.strong.inject

/**
 * Health controller
 */
class SimpleHealthController(strong: Strong) : Strong.LinkingBean {
    private val router by strong.inject<Route>()

    /**
     * Method will be call on link step
     */
    override suspend fun link(strong: Strong) {
        /**
         * Defining endpoint
         */
        router.get("/health") {
            it.response {
                it.status = 200
            }
        }
    }
}

val COMMON_CONFIG = Strong.config { definer ->
    //Define root router of flux
    definer.bean { RootRouter() }

    //Define simple health controller
    definer.bean { SimpleHealthController(it) }
}

val PRODUCTION_CONFIG = Strong.config { definer ->
    //Defining http starter bean
    definer.bean { HttpServerProvider(it) }
}

class HttpServerProvider(strong: Strong) : Strong.LinkingBean, Strong.DestroyableBean {
    //Injecting NetworkDispatcher. It was auto-defined with StringApplication
    private val nd by strong.inject<NetworkDispatcher>()

    //Injecting RootRouter
    private val router by strong.inject<Handler>()
    private lateinit var server: HttpServer

    /**
     * Starting http server on strong start
     */
    override suspend fun link(strong: Strong) {
        server = HttpServer(
            manager = nd,
            handler = router
        )
    }

    /**
     * Stopping http server on stop strong
     */
    override suspend fun destroy(strong: Strong) {
        server.asyncClose()
    }
}

fun main() {
    /**
     * Define NetworkDispatcher and starts Strong Application.
     * [StrongApplication.start] receive array of configuration for start
     */
    StrongApplication.start(
        COMMON_CONFIG,
        PRODUCTION_CONFIG,
    )
}