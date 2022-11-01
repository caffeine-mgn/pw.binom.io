package pw.binom.metric

interface Gauge : MetricUnit {
    val value: Double
}
