package pw.binom.metric

class DoubleRollingAverageGauge(
    fields: Map<String, String> = emptyMap(),
    name: String,
    description: String? = null,
    windowSize: Int,
) : AbstractRollingAverageGauge(
    fields = fields,
    name = name,
    description = description,
    windowSize = windowSize,
) {
    public override fun put(value: Double) = super.put(value)
}
