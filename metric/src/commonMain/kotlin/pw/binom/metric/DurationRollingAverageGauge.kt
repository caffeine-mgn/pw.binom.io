package pw.binom.metric

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class DurationRollingAverageGauge(
    fields: Map<String, String> = emptyMap(),
    name: String,
    description: String? = null,
    windowSize: Int,
    val unit: DurationUnit
) : AbstractRollingAverageGauge(
    fields = fields,
    name = name,
    description = description,
    windowSize = windowSize,
) {
    fun put(duration: Duration) {
        put(duration.toLong(unit).toDouble())
    }

    val valueDuration: Duration
        get() = value.toDuration(unit)
}
