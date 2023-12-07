package pw.binom.io.socket

import pw.binom.BinomMetrics
import pw.binom.metric.MutableLongGauge

object NetworkMetrics {
  val selectorKeyCountMetric =
    MutableLongGauge("binom_selector_key_count", description = "SelectorKey Count")
  val selectorKeyAllocCountMetric =
    MutableLongGauge("binom_selector_key_alloc_count", description = "SelectorKey Alloc Count")

  fun incSelectorKey() {
    selectorKeyCountMetric.inc()
  }

  fun decSelectorKey() {
    selectorKeyCountMetric.dec()
  }

  fun incSelectorKeyAlloc() {
    selectorKeyAllocCountMetric.inc()
  }

  fun decSelectorKeyAlloc() {
    selectorKeyAllocCountMetric.dec()
  }

  init {
    BinomMetrics.reg(selectorKeyCountMetric)
    BinomMetrics.reg(selectorKeyAllocCountMetric)
  }
}
