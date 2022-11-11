package pw.binom.db.serialization

import pw.binom.BinomMetrics
import pw.binom.metric.DurationRollingAverageGauge
import kotlin.time.DurationUnit

object SerializationMetrics {
    internal val encodeAvrTime = DurationRollingAverageGauge(
        name = "binom_db_serialization_encode",
        windowSize = 30,
        unit = DurationUnit.NANOSECONDS
    )
    internal val decodeAvrTime = DurationRollingAverageGauge(
        name = "binom_db_serialization_decode",
        windowSize = 30,
        unit = DurationUnit.NANOSECONDS
    )

    init {
        BinomMetrics.reg(encodeAvrTime)
        BinomMetrics.reg(decodeAvrTime)
    }
}
