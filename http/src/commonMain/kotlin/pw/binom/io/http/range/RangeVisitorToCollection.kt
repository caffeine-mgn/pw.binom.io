package pw.binom.io.http.range

class RangeVisitorToCollection(val dest: MutableList<Range>) : RangeVisitor, RangeUnitVisitor {
    private var rangeUnit = ""

    override fun start(position: Long) {
        dest.add(Range.StartFrom(rangeUnit, start = position))
    }

    override fun between(start: Long, end: Long) {
        dest.add(Range.Between(rangeUnit, start = start, end = end))
    }

    override fun last(size: Long) {
        dest.add(Range.Last(rangeUnit, size = size))
    }

    override fun startParse(unit: String): RangeVisitor {
        rangeUnit = unit
        return this
    }
}
