package pw.binom.io.httpServer

import pw.binom.BinomMetrics
import pw.binom.metric.MutableLongGauge

object HttpServerMetrics {
    internal val idleHttpServerConnection =
        MutableLongGauge("binom_http_server_idle_connection", description = "HttpServer Idle Connections")
    internal val httpRequestCounter =
        MutableLongGauge("binom_http_request_counter", description = "HttpServer Request Counter")

    init {
        BinomMetrics.reg(idleHttpServerConnection)
        BinomMetrics.reg(httpRequestCounter)
    }
}
