package pw.binom.process

/**
 * For work in java run as "java -Xrs -jar <path to you program>"
 */
object Signal {
    fun handler(func: (Type) -> Unit) {
        addSignalListener(func)
    }

    enum class Type(val isInterrupted: Boolean) {
        Sigint(isInterrupted = true),
        Sigbreak(isInterrupted = true),
        Sigterm(isInterrupted = true),
        Close(isInterrupted = true),
        Logoff(isInterrupted = true),
        Shutdown(isInterrupted = true),
    }
}

internal expect fun addSignalListener(func: (Signal.Type) -> Unit)