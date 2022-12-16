package pw.binom.metric

abstract class AbstractRollingAverageGauge(
    override val fields: Map<String, String> = emptyMap(),
    override val name: String,
    override val description: String? = null,
    windowSize: Int,
) : GaugeDouble {
    init {
        require(windowSize > 1) { "windowSize should be more than 1 " }
    }

    internal var values = DoubleArray(windowSize)
    internal var size = 0
    internal var cursor = 0
    private var toLeft = true
    protected open fun put(value: Double) {
        if (size < values.size) {
            values[cursor++] = value
            size++
            if (cursor == values.size) {
                toLeft = false
                cursor = values.size - 2
            }
            return
        }

        if (toLeft) {
            values[cursor++] = value
            if (cursor >= values.size) {
                cursor = values.size - 2
                toLeft = false
            }
        } else {
            values[cursor--] = value
            if (cursor < 0) {
                cursor = 1
                toLeft = true
            }
        }
    }

    protected open fun convertResultValue(value: Double): Double = value

    override val value: Double
        get() {
            if (size == 0) {
                return 0.0
            }
            var total = 0.0
            for (i in 0 until size) {
                total += values[i]
            }
            if (total == 0.0) {
                return 0.0
            }
            return convertResultValue(total / size)
        }
}
