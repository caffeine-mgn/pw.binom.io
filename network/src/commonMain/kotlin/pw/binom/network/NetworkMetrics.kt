package pw.binom.network

import pw.binom.BinomMetrics
import pw.binom.metric.MutableGauge

internal object NetworkMetrics {
    private val selectorKeyCountMetric = MutableGauge("binom_selector_key_count", description = "SelectorKey Count")
    fun incSelectorKey() {
        selectorKeyCountMetric.inc()
    }

    fun decSelectorKey() {
        selectorKeyCountMetric.dec()
    }

    init {
        BinomMetrics.reg(selectorKeyCountMetric)
    }
}
