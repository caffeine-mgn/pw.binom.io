package pw.binom.process

import kotlinx.cinterop.staticCFunction
import platform.posix.SIGINT
import platform.posix.SIGTERM
import kotlin.native.concurrent.AtomicInt

private val signalListener = staticCFunction<Int, Unit> handler@{ signal ->
    initRuntimeIfNeeded()
    println("signal: $signal")
    when (signal) {
        SIGINT -> Signal._isSigint.value = 1
        platform.linux.internal_SIGBREAK -> Signal._isSigbreak.value = 1
        SIGTERM -> Signal._isSigterm.value = 1
    }
    return@handler
}

actual object Signal {
    internal val _isSigint = AtomicInt(0)
    internal val _isSigbreak = AtomicInt(0)
    internal val _isSigterm = AtomicInt(0)

    actual val isSigint: Boolean
        get() = _isSigint.value == 1

    actual val isSigbreak: Boolean
        get() = _isSigbreak.value == 1

    actual val isSigterm: Boolean
        get() = _isSigterm.value == 1

    actual val isInterrupted: Boolean
        get() = isSigint || isSigbreak || isSigterm

    actual val isClose: Boolean
        get() = false
    actual val isLogoff: Boolean
        get() = false
    actual val isShutdown: Boolean
        get() = false

    init {
        listen2(SIGINT)
        listen2(platform.linux.internal_SIGBREAK)
        listen2(SIGTERM)
    }

    private fun listen2(signal: Int) {
        platform.posix.signal(signal, signalListener)
    }
}