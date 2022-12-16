package pw.binom.metric

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class MetricProviderImpl : MetricProvider, ReadOnlyProperty<Any?, List<MetricUnit>> {
    private val metricUnits = ArrayList<MetricUnit>()
    override val metrics: List<MetricUnit>
        get() = metricUnits

    fun counterDouble(
        name: String,
        description: String? = null,
        fields: Map<String, String> = emptyMap(),
    ): MutableDoubleCounter {
        val unit = MutableDoubleCounter(name = name, description = description, fields = fields)
        metricUnits += unit
        return unit
    }

    fun counterLong(
        name: String,
        description: String? = null,
        fields: Map<String, String> = emptyMap(),
    ): MutableLongCounter {
        val unit = MutableLongCounter(name = name, description = description, fields = fields)
        metricUnits += unit
        return unit
    }

    fun gaugeDouble(
        name: String,
        description: String? = null,
        fields: Map<String, String> = emptyMap(),
    ): MutableDoubleGauge {
        val unit = MutableDoubleGauge(name = name, description = description, fields = fields)
        metricUnits += unit
        return unit
    }

    fun gaugeLong(
        name: String,
        description: String? = null,
        fields: Map<String, String> = emptyMap(),
    ): MutableLongGauge {
        val unit = MutableLongGauge(name = name, description = description, fields = fields)
        metricUnits += unit
        return unit
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): List<MetricUnit> = metrics
}
