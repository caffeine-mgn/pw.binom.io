package pw.binom.io.http.range

fun List<Range>.toHeader(): String {
    if (isEmpty()) {
        throw IllegalArgumentException("Range list is empty")
    }
    val sb = StringBuilder()
    val it = iterator()
    val first = it.next()
    val visitor = RangeVisitorWriter(sb).startParse(first.unit)
    first.accept(visitor)

    it.forEach { item ->
        if (item.unit != first.unit) {
            throw IllegalArgumentException("Can't write different range units in one range header")
        }
        item.accept(visitor)
    }
    return sb.toString()
}
