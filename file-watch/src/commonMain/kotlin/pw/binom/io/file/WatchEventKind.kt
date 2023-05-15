package pw.binom.io.file

import kotlin.jvm.JvmInline

@JvmInline
value class WatchEventKind(@PublishedApi internal val raw: Int) {
    inline infix fun or(other: WatchEventKind) = WatchEventKind(raw or other.raw)
    inline operator fun plus(other: WatchEventKind) = this or other

    companion object {
        private const val RAW_IN_MODIFY = 0b001
        private const val RAW_IN_CREATE = 0b010
        private const val RAW_IN_DELETE = 0b100

        val EMPTY = WatchEventKind(0)
        val MODIFY = WatchEventKind(RAW_IN_MODIFY)
        val CREATE = WatchEventKind(RAW_IN_CREATE)
        val DELETE = WatchEventKind(RAW_IN_DELETE)
        val ALL = MODIFY + CREATE + DELETE
    }

    val isEmpty
        get() = raw and 0b111 == 0

    val isModify
        get() = raw and RAW_IN_MODIFY != 0

    val isCreate
        get() = raw and RAW_IN_CREATE != 0

    val isDelete
        get() = raw and RAW_IN_DELETE != 0

    val size: Int
        get() {
            var r = 0
            if (isModify) {
                r++
            }
            if (isCreate) {
                r++
            }
            if (isDelete) {
                r++
            }
            return r
        }

    inline fun forEach(func: (WatchEventKind) -> Unit) {
        if (isModify) {
            func(MODIFY)
        }
        if (isCreate) {
            func(CREATE)
        }
        if (isDelete) {
            func(DELETE)
        }
    }

    inline fun forEachIndexed(func: (Int, WatchEventKind) -> Unit) {
        var i = 0
        if (isModify) {
            func(i++, MODIFY)
        }
        if (isCreate) {
            func(i++, CREATE)
        }
        if (isDelete) {
            func(i++, DELETE)
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        if (isModify) {
            sb.append("MODIFY")
        }
        if (isCreate) {
            if (sb.isNotEmpty()) {
                sb.append(" and ")
            }
            sb.append("CREATE")
        }
        if (isDelete) {
            if (sb.isNotEmpty()) {
                sb.append(" and ")
            }
            sb.append("DELETE")
        }
        return sb.toString()
    }
}
