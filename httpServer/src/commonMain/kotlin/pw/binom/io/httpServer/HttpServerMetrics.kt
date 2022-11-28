package pw.binom.io.httpServer

import pw.binom.BinomMetrics
import pw.binom.metric.MutableGauge

object HttpServerMetrics {
    internal val idleHttpServerConnection =
        MutableGauge("binom_http_server_idle_connection", description = "HttpServer Idle Connections")

    init {
        BinomMetrics.reg(idleHttpServerConnection)
    }
}
