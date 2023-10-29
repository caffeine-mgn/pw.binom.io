package pw.binom

import pw.binom.io.Closeable
import pw.binom.metric.DurationRollingAverageGauge
import pw.binom.metric.MutableLongGauge
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
@Deprecated(message = "Used for find cycle loop. Not use")
class LoopWatcher(name: String) : Closeable {
  val metricName = name.lowercase().replace(' ', '_').replace('-', '_')
  private val avgGauge = DurationRollingAverageGauge(
    name = "${metricName}_avr",
    unit = DurationUnit.MILLISECONDS,
    windowSize = 10,
  )
  private val countMetric = MutableLongGauge(name = "${metricName}_count")
  private var lastCall = TimeSource.Monotonic.markNow()
  private val reg = BinomMetrics.reg(avgGauge)
  private val reg2 = BinomMetrics.reg(countMetric)

  fun call() {
    countMetric.inc()
    avgGauge.put(lastCall.elapsedNow())
    lastCall = TimeSource.Monotonic.markNow()
  }

  override fun close() {
    Closeable.close(reg, reg2)
  }
}
