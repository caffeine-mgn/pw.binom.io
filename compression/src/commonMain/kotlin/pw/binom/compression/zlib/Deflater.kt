package pw.binom.compression.zlib

import pw.binom.BinomMetrics
import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable
import pw.binom.metric.MutableLongGauge

internal object DeflaterMetrics {
    private val deflaterCount = MutableLongGauge("binom_deflater_deflater_count", description = "Deflater Count")
    private val inflaterCount = MutableLongGauge("binom_deflater_inflater_count", description = "Inflater Count")
    fun incDeflaterCount() {
        deflaterCount.inc()
    }

    fun decDeflaterCount() {
        deflaterCount.dec()
    }

    fun incInflaterCount() {
        inflaterCount.inc()
    }

    fun decInflaterCount() {
        inflaterCount.dec()
    }

    init {
        BinomMetrics.reg(deflaterCount)
        BinomMetrics.reg(inflaterCount)
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect class Deflater : Closeable {
    constructor(level: Int = 6, wrap: Boolean = true, syncFlush: Boolean = true)

    val totalIn: Long
    val totalOut: Long

    fun end()
    val finished: Boolean
    fun finish()

    fun deflate(input: ByteBuffer, output: ByteBuffer): Int

    /**
     * Flush changes
     *
     * @return true - you must recall this method again
     */
    fun flush(output: ByteBuffer): Boolean
}
