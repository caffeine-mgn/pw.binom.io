package pw.binom.charset

import pw.binom.BinomMetrics
import pw.binom.metric.MutableGauge

internal object CharsetMetrics {
    private val encoderCount = MutableGauge("binom_charset_encoder_count", description = "Charset Encoder Count")
    private val decoderCount = MutableGauge("binom_charset_decoder_count", description = "Charset Decoder Count")
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
