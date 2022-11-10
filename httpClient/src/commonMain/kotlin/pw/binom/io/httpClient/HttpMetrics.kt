package pw.binom.io.httpClient

import pw.binom.BinomMetrics
import pw.binom.metric.MutableGauge

internal object HttpMetrics {
    val baseHttpClientCountMetric = MutableGauge("binom_base_http_client", description = "BaseHttpClient Count")
    val defaultHttpRequestCountMetric =
        MutableGauge("binom_default_http_request", description = "DefaultHttpRequest Count")

    init {
        BinomMetrics.reg(baseHttpClientCountMetric)
    }
}
