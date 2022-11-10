package pw.binom.io.socket.ssl

import pw.binom.BinomMetrics
import pw.binom.metric.MutableGauge

internal object SSLMetrics {
    private val sslCountMetric = MutableGauge("binom_ssl_session_count", description = "SSLSession Count")
    val asyncSSLChannelCountMetric = MutableGauge("binom_ssl_async_channel", description = "AsyncSSLChannel Count")
    fun incSSLSession() {
        sslCountMetric.inc()
    }

    fun decSSLSession() {
        sslCountMetric.dec()
    }

    init {
        BinomMetrics.reg(sslCountMetric)
    }
}
