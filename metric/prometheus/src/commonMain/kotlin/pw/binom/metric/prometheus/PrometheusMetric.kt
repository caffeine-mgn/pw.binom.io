package pw.binom.metric.prometheus

import pw.binom.metric.MetricType
import pw.binom.metric.MetricVisitor

data class PrometheusMetric(
  val name: String,
  val help: String?,
  val type: MetricType?,
  val value: String,
  val fields: Map<String, String>,
) {
  companion object {
    fun readTo(list: MutableCollection<PrometheusMetric>) = object : MetricVisitor {
      private var fields = HashMap<String, String>()
      private var name: String = ""
      private var value: String = ""
      private var lastHelp: String? = null
      private var lastType: MetricType? = null

      override fun start(name: String) {
        this.name = name
      }

      override fun help(name: String, text: String) {
        lastHelp = text
      }

      override fun type(name: String, type: MetricType) {
        lastType = type
      }

      override fun field(name: String, value: String) {
        fields[name] = value
      }

      override fun value(value: String) {
        this.value = value
      }

      override fun end() {
        list += PrometheusMetric(
          name = name,
          help = lastHelp,
          type = lastType,
          value = value,
          fields = if (fields.isEmpty()) {
            emptyMap()
          } else {
            val v = fields
            fields = HashMap()
            v
          }
        )
        lastHelp = null
        lastType = null
        name = ""
        value = ""
      }
    }
  }
}
