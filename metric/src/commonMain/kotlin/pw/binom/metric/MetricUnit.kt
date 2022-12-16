package pw.binom.metric

sealed interface MetricUnit {
    val fields: Map<String, String>
    val name: String
    val description: String?
}
