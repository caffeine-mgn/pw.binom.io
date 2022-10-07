package pw.binom.io.http.range

fun interface RangeUnitVisitor {
    fun startParse(unit: String): RangeVisitor
}
