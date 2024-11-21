package pw.binom.metric.prometheus

import pw.binom.metric.AsyncMetricVisitor
import pw.binom.metric.MetricType
import pw.binom.metric.MetricVisitor

suspend fun Collection<PrometheusMetric>.accept(visitor: AsyncMetricVisitor) {
  forEach { metric ->
    val help = metric.help
    val type = metric.type
    if (help != null) {
      visitor.help(name = metric.name, text = help)
    }
    if (type != null) {
      visitor.type(name = metric.name, type = type)
    }
    visitor.start(metric.name)
    metric.fields.forEach { (name, value) ->
      visitor.field(name = name, value = value)
    }
    visitor.value(metric.value)
    visitor.end()
  }
}

fun Collection<PrometheusMetric>.accept(visitor: MetricVisitor) {
  forEach { metric ->
    val help = metric.help
    val type = metric.type
    visitor.start(metric.name)
    if (help != null) {
      visitor.help(name = metric.name, text = help)
    }
    if (type != null) {
      visitor.type(name = metric.name, type = type)
    }
    metric.fields.forEach { (name, value) ->
      visitor.field(name = name, value = value)
    }
    visitor.value(metric.value)
    visitor.end()
  }
}

fun MutableCollection<PrometheusMetric>.add(
  name: String,
  value: String,
  help: String? = null,
  type: MetricType? = null,
  fields: Map<String, String> = emptyMap(),
): PrometheusMetric {
  val metric = PrometheusMetric(
    name = name,
    help = help,
    type = type,
    value = value,
    fields = fields
  )
  this += metric
  return metric
}
