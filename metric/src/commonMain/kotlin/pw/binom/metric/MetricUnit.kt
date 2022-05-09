package pw.binom.metric

interface MetricUnit {
    val fields: Map<String, String>
    val name: String
    val description: String?
}
