package pw.binom.flux

import pw.binom.BinomMetrics
import pw.binom.metric.MutableLongGauge

object WebMetrics {
    internal val cachingHttpRequest =
        MutableLongGauge("binom_caching_http_request_count", description = "CachingHttpRequest Count")
    internal val cachingHttpResponse =
        MutableLongGauge("binom_caching_http_response", description = "CachingHttpResponse Count")
    internal val fluxHttpRequest =
        MutableLongGauge("binom_flux_http_request_impl", description = "FluxHttpRequestImpl Count")

    init {
        BinomMetrics.reg(cachingHttpRequest)
        BinomMetrics.reg(cachingHttpResponse)
        BinomMetrics.reg(fluxHttpRequest)
    }
}
