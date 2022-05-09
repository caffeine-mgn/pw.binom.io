package pw.binom.metric

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class MetricProviderImpl : MetricProvider, ReadOnlyProperty<Any?, List<MetricUnit>> {
    private val metricUnits = ArrayList<MetricUnit>()
    override val metrics: List<MetricUnit>
        get() = metricUnits

    fun counter(
        name: String,
        description: String? = null,
        fields: Map<String, String> = emptyMap(),
    ): Counter {
        val unit = Counter(name = name, description = description, fields = fields)
        metricUnits += unit
        return unit
    }

    fun gauge(
        name: String,
        description: String? = null,
        fields: Map<String, String> = emptyMap(),
    ): Gauge {
        val unit = Gauge(name = name, description = description, fields = fields)
        metricUnits += unit
        return unit
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): List<MetricUnit> = metrics
}
