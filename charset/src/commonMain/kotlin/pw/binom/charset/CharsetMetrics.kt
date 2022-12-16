package pw.binom.charset

import pw.binom.BinomMetrics
import pw.binom.metric.MutableLongGauge

internal object CharsetMetrics {
    private val encoderCount = MutableLongGauge("binom_charset_encoder_count", description = "Charset Encoder Count")
    private val decoderCount = MutableLongGauge("binom_charset_decoder_count", description = "Charset Decoder Count")
    fun incEncoder() {
        encoderCount.inc()
    }

    fun decEncoder() {
        encoderCount.dec()
    }

    fun incDecoder() {
        decoderCount.inc()
    }

    fun decDecoder() {
        decoderCount.dec()
    }

    init {
        BinomMetrics.reg(encoderCount)
        BinomMetrics.reg(decoderCount)
    }
}
