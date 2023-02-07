package pw.binom.metric.prometheus

import pw.binom.io.AsyncAppendable
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.metric.*

abstract class AbstractPrometheusHandler : Handler {
    protected abstract suspend fun getMetrics(): List<MetricProvider>

    protected open suspend fun getDefaultFields(): Map<String, String> = emptyMap()

    override suspend fun request(req: HttpRequest) {
        val metrics = getMetrics()
        val defaultFields = getDefaultFields()
        req.response {
            it.status = 200
            it.sendText { writer ->
                var first = true
                metrics.forEach { provider ->
                    provider.metrics.forEach { group ->
                        if (!first) {
                            writer.append("\n")
                        }
                        if (group.description != null) {
                            writer.append("# HELP ")
                                .append(group.name)
                                .append(" ")
                                .append(group.description)
                                .append("\n")
                        }
                        val type = when (group) {
                            is Counter -> "counter"
                            is Gauge -> "gauge"
                            else -> TODO()
                        }
                        writer.append("# TYPE ")
                            .append(group.name)
                            .append(" ")
                            .append(type)
                            .append("\n")
                        when (group) {
                            is CounterDouble -> generatePrometheus(
                                appendable = writer,
                                name = group.name,
                                value = group.value,
                                fields = group.fields + defaultFields,
                            )

                            is GaugeDouble -> generatePrometheus(
                                appendable = writer,
                                name = group.name,
                                value = group.value,
                                fields = group.fields + defaultFields,
                            )

                            is CounterLong -> generatePrometheus(
                                appendable = writer,
                                name = group.name,
                                value = group.value,
                                fields = group.fields + defaultFields,
                            )

                            is GaugeLong -> generatePrometheus(
                                appendable = writer,
                                name = group.name,
                                value = group.value,
                                fields = group.fields + defaultFields,
                            )
                        }
                        first = false
                    }
                }
            }
        }
    }
}

private suspend fun generatePrometheus(
    appendable: AsyncAppendable,
    name: String,
    value: Long,
    fields: Map<String, String>
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

private suspend fun generatePrometheus(
    appendable: AsyncAppendable,
    name: String,
    value: Double,
    fields: Map<String, String>
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
