package pw.binom.io.httpClient

import pw.binom.BinomMetrics
import pw.binom.metric.MutableLongGauge

internal object HttpMetrics {
    val baseHttpClientCountMetric = MutableLongGauge("binom_base_http_client", description = "BaseHttpClient Count")
    val defaultHttpRequestCountMetric =
        MutableLongGauge("binom_default_http_request", description = "DefaultHttpRequest Count")

    init {
        BinomMetrics.reg(baseHttpClientCountMetric)
        BinomMetrics.reg(defaultHttpRequestCountMetric)
    }
}
