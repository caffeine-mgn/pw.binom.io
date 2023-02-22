package pw.binom.metric.prometheus

import pw.binom.io.AsyncAppendable
import pw.binom.metric.*

object PrometheusResponseGenerator {

    private suspend fun generatePrometheus(
        appendable: AsyncAppendable,
        name: String,
        value: Double,
        fields: Map<String, String>,
    ) {
        appendable.append(name)
        var first = true
        fields.forEach { (k, v) ->
            if (first) {
                appendable.append("{")
            } else {
                appendable.append(",")
            }
            appendable.append(k).append("=\"").append(v).append("\"")
            first = false
        }
        if (fields.isNotEmpty()) {
            appendable.append("}")
        }
        appendable.append(" ").append(value.toString())
    }

    suspend fun generate(metric: MetricProvider, dest: AsyncAppendable, defaultFields: Map<String, String>) {
        generate(metric = metric.metrics, dest = dest, defaultFields = defaultFields)
    }

    suspend fun generate(metric: Iterable<MetricUnit>, dest: AsyncAppendable, defaultFields: Map<String, String>) {
        var first = true
        metric.forEach { group ->
            if (!first) {
                dest.append("\n")
            }
            generate(metric = group, dest = dest, defaultFields = defaultFields)
            first = false
        }
    }

    suspend fun generate(metric: MetricUnit, dest: AsyncAppendable, defaultFields: Map<String, String>) {
        if (metric.description != null) {
            dest.append("# HELP ").append(metric.name).append(" ").append(metric.description).append("\n")
        }
        val type = when (metric) {
            is Counter -> "counter"
            is Gauge -> "gauge"
            else -> TODO()
        }
        dest.append("# TYPE ").append(metric.name).append(" ").append(type).append("\n")
        when (metric) {
            is CounterDouble -> generatePrometheus(
                appendable = dest,
                name = metric.name,
                value = metric.value,
                fields = metric.fields + defaultFields,
            )

            is GaugeDouble -> generatePrometheus(
                appendable = dest,
                name = metric.name,
                value = metric.value,
                fields = metric.fields + defaultFields,
            )

            is CounterLong -> generatePrometheus(
                appendable = dest,
                name = metric.name,
                value = metric.value,
                fields = metric.fields + defaultFields,
            )

            is GaugeLong -> generatePrometheus(
                appendable = dest,
                name = metric.name,
                value = metric.value,
                fields = metric.fields + defaultFields,
            )
        }
    }

    private suspend fun generatePrometheus(
        appendable: AsyncAppendable,
        name: String,
        value: Long,
        fields: Map<String, String>,
    ) {
        appendable.append(name)
        var first = true
        fields.forEach { (k, v) ->
            if (first) {
                appendable.append("{")
            } else {
                appendable.append(",")
            }
            appendable.append(k).append("=\"").append(v).append("\"")
            first = false
        }
        if (fields.isNotEmpty()) {
            appendable.append("}")
        }
        appendable.append(" ").append(value.toString())
    }
}
