package pw.binom.io.http.range

interface RangeVisitor {
    fun start(position: Long)
    fun between(start: Long, end: Long)
    fun last(position: Long)
}
