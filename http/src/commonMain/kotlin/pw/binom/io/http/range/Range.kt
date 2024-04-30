package pw.binom.io.http.range

sealed interface Range {
    val unit: String
    fun accept(visitor: RangeVisitor)

    data class StartFrom(override val unit: String, val start: Long) : Range {
        override fun accept(visitor: RangeVisitor) {
            visitor.start(start)
        }

        override fun toString(): String = "$unit=$start-"
    }

    data class Between(override val unit: String, val start: Long, val end: Long) : Range {
        override fun accept(visitor: RangeVisitor) {
            visitor.between(
                start = start,
                end = end
            )
        }

        override fun toString(): String = "$unit=$start-$end"
    }

    data class Last(override val unit: String, val size: Long) : Range {
        override fun accept(visitor: RangeVisitor) {
            visitor.last(size)
        }

        override fun toString(): String = "$unit=-$size"
    }

    companion object {
        const val BYTES_UNIT = "bytes"
        fun parseRange(value: String, visitor: RangeUnitVisitor) {
            val unitSeparatorPosition = value.indexOf('=')
            val unit = value.substring(0, unitSeparatorPosition)
            val rangeVisitor = visitor.startParse(unit)
            var rangeStart = unitSeparatorPosition + 1
            while (true) {
                val separator = value.indexOf('-', rangeStart)
                if (separator == -1) {
                    throw IllegalArgumentException("Invalid range format: ${value.substring(rangeStart)}")
                }
                val rangeEnd =
                    value.indexOf(',', startIndex = rangeStart + 1).let { if (it == -1) value.length else it }
                when {
                    rangeStart == separator -> { // last bytes of data
                        val end = value.substring(startIndex = separator + 1, endIndex = rangeEnd)
                        rangeVisitor.last(end.toULong().toLong())
                    }

                    separator + 1 == rangeEnd -> {
                        val start = value.substring(rangeStart, separator)
                        rangeVisitor.start(start.toULong().toLong())
                    }

                    else -> {
                        val start = value.substring(rangeStart, separator)
                        val end = value.substring(separator + 1, rangeEnd)
                        rangeVisitor.between(start = start.toULong().toLong(), end = end.toULong().toLong())
                    }
                }
                rangeStart = rangeEnd + 1
                if (rangeStart >= value.length) {
                    break
                }
            }
        }

        fun parseRange(value: String, dest: MutableList<Range>) {
            parseRange(value = value, visitor = RangeVisitorToCollection(dest))
        }

        fun parseRange(value: String): List<Range> {
            val out = ArrayList<Range>()
            parseRange(value = value, dest = out)
            return out
        }
    }
}
