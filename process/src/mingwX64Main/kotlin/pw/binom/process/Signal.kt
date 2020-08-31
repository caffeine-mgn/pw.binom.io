package pw.binom.process

import kotlinx.cinterop.staticCFunction
import platform.windows.*
import kotlin.native.concurrent.AtomicInt

actual object Signal {

    internal val _isSigint = AtomicInt(0)
    internal val _isSigbreak = AtomicInt(0)
    internal val _isSigterm = AtomicInt(0)
    internal val _isLogoff = AtomicInt(0)
    internal val _isClose = AtomicInt(0)
    internal val _isShutdown = AtomicInt(0)

    actual val isSigint: Boolean
        get() = _isSigint.value == 1

    actual val isSigbreak: Boolean
        get() = _isSigbreak.value == 1

    actual val isSigterm: Boolean
        get() = _isSigterm.value == 1

    actual val isClose: Boolean
        get() = _isClose.value == 1
    actual val isLogoff: Boolean
        get() = _isLogoff.value == 1
    actual val isShutdown: Boolean
        get() = _isShutdown.value == 1

    actual val isInterrupted: Boolean
        get() = isSigint || isSigbreak || isSigterm || isClose || isShutdown || isLogoff

    init {
        SetConsoleCtrlHandler(signalHandler, TRUE)
    }
}

private val signalHandler = staticCFunction<DWORD, WINBOOL> handler@{ signal ->
    initRuntimeIfNeeded()
    when (signal.toInt()) {
        CTRL_C_EVENT -> Signal._isSigint.value = 1
        CTRL_BREAK_EVENT -> Signal._isSigbreak.value = 1
        CTRL_CLOSE_EVENT -> Signal._isSigterm.value = 1
        CTRL_LOGOFF_EVENT -> Signal._isLogoff.value = 1
        CTRL_SHUTDOWN_EVENT -> Signal._isShutdown.value = 1
        else -> return@handler FALSE
    }
    return@handler TRUE
}