package pw.binom.signal

import kotlinx.cinterop.staticCFunction
import platform.posix.usleep
import platform.windows.*
import kotlin.native.concurrent.AtomicInt

private var signalListening = AtomicInt(0)
private val listeners = ArrayList<(Signal.Type) -> Unit>()
private val listenersLock = AtomicInt(0)

private fun lockListeners() {
    while (true) {
        if (listenersLock.compareAndSet(0, 1)) {
            break
        }
        usleep((1 * 1000).toUInt())
    }
}

internal actual fun addSignalListener(func: (Signal.Type) -> Unit) {
    if (signalListening.compareAndSet(0, 1)) {
        SetConsoleCtrlHandler(signalHandler, TRUE)
    }
    lockListeners()
    listeners += func
    listenersLock.value = 0
}

private val signalHandler = staticCFunction<DWORD, WINBOOL> handler@{ signal ->
    initRuntimeIfNeeded()
    val type = when (signal.toInt()) {
        CTRL_C_EVENT -> Signal.Type.Sigint
        CTRL_BREAK_EVENT -> Signal.Type.Sigbreak
        CTRL_CLOSE_EVENT -> Signal.Type.Sigterm
        CTRL_LOGOFF_EVENT -> Signal.Type.Logoff
        CTRL_SHUTDOWN_EVENT -> Signal.Type.Logoff
        else -> return@handler FALSE
    }

    lockListeners()
    try {
        listeners.forEach {
            try {
                it(type)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    } finally {
        listenersLock.value = 0
    }
    return@handler TRUE
}
