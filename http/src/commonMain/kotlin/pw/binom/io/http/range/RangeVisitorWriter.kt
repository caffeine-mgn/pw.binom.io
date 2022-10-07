package pw.binom.io.http.range

class RangeVisitorWriter(val appendable: Appendable) : RangeVisitor, RangeUnitVisitor {
    private var rangeUnit = ""
    private var first = true
    override fun startParse(unit: String): RangeVisitor {
        rangeUnit = unit
        return this
    }

    private fun prepare() {
        if (!first) {
            appendable.append(", ")
        } else {
            first = false
            appendable.append(rangeUnit).append("=")
        }
    }

    override fun start(position: Long) {
        prepare()
        appendable.append(position.toString()).append("-")
    }

    override fun between(start: Long, end: Long) {
        prepare()
        appendable.append(start.toString()).append("-").append(end.toString())
    }

    override fun last(position: Long) {
        prepare()
        appendable.append("-").append(position.toString())
    }
}
