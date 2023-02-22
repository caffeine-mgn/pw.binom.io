package pw.binom.io

import pw.binom.BinomMetrics
import pw.binom.metric.MutableLongGauge

object ByteBufferMetric {
    val BYTEBUFFER_COUNT_METRIC =
        MutableLongGauge("binom_byte_buffer_count", description = "ByteBuffer Count")
    val BYTEBUFFER_MEMORY_METRIC =
        MutableLongGauge("binom_byte_buffer_memory", description = "ByteBuffer Memory")

    fun inc(buffer: ByteBuffer) {
        BYTEBUFFER_COUNT_METRIC.inc()
        BYTEBUFFER_MEMORY_METRIC.inc(buffer.capacity)
    }

    fun dec(buffer: ByteBuffer) {
        BYTEBUFFER_MEMORY_METRIC.dec(buffer.capacity)
        BYTEBUFFER_COUNT_METRIC.dec()
    }

    init {
        BinomMetrics.reg(BYTEBUFFER_COUNT_METRIC)
        BinomMetrics.reg(BYTEBUFFER_MEMORY_METRIC)
    }
}
