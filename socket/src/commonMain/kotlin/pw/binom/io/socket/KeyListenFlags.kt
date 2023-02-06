package pw.binom.io.socket

object KeyListenFlags {
    const val READ = 0b0001
    const val WRITE = 0b0010
    const val ERROR = 0b0100
    const val ONCE = 0b1000

    internal fun toString(flags: Int): String {
        val sb = StringBuilder()
        var started = false
        fun start() {
            if (!started) {
                started = true
            } else {
                sb.append(" ")
            }
        }
        if (flags and READ != 0) {
            start()
            sb.append("READ")
        }
        if (flags and WRITE != 0) {
            start()
            sb.append("WRITE")
        }
        if (flags and ERROR != 0) {
            start()
            sb.append("ERROR")
        }
        if (flags and ONCE != 0) {
            start()
            sb.append("ONCE")
        }
        sb.append(" ${flags.toUInt().toString(16)}")
        return sb.toString()
    }
}
