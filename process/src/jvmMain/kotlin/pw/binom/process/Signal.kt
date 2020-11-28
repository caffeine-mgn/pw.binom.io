package pw.binom.process

import sun.misc.SignalHandler
import java.util.concurrent.atomic.AtomicBoolean
import sun.misc.Signal as JSignal

private val globalHandler = SignalHandler { sig ->
    when (sig.name) {
        "INT" -> Signal._isSigint.set(true)
        "TERM" -> Signal._isSigterm.set(true)
    }
}

actual object Signal {
    private const val NOT_SUPPORTED_CODE = ""

    internal val _isSigint = AtomicBoolean(false)
    internal val _isSigterm = AtomicBoolean(false)
    internal val _isInterrupted = AtomicBoolean(false)

    actual val isSigint: Boolean
        get() = _isSigint.get()

    actual val isSigbreak: Boolean
        get() = false

    actual val isSigterm: Boolean
        get() = _isSigterm.get()

    actual val isInterrupted: Boolean
        get() = _isInterrupted.get() || isSigint || isSigterm

    actual val isClose: Boolean
        get() = false
    actual val isLogoff: Boolean
        get() = false
    actual val isShutdown: Boolean
        get() = false

    init {
        Runtime.getRuntime().addShutdownHook(Thread(Runnable {
            _isInterrupted.set(true)
        }))
        JSignal.handle(JSignal("INT"), globalHandler)
        JSignal.handle(JSignal("TERM"), globalHandler)
    }
}