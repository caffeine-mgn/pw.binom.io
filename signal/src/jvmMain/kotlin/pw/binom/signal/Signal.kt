@file:JvmName("SignalJvmKt")

package pw.binom.signal

import sun.misc.SignalHandler
import java.util.concurrent.atomic.AtomicBoolean
import sun.misc.Signal as JSignal

private var signalListening = AtomicBoolean(false)
private val listeners = ArrayList<(Signal.Type) -> Unit>()
private val listenersLock = AtomicBoolean(false)

private fun lockListeners() {
    while (true) {
        if (listenersLock.compareAndSet(false, true)) {
            break
        }
        Thread.sleep(1)
    }
}

internal actual fun addSignalListener(func: (Signal.Type) -> Unit) {
    if (signalListening.compareAndSet(false, true)) {
        JSignal.handle(JSignal("INT"), globalHandler)
        JSignal.handle(JSignal("TERM"), globalHandler)
    }
    lockListeners()
    listeners += func
    listenersLock.set(false)
}

private val globalHandler = SignalHandler { sig ->
    val type = when (sig.name) {
        "INT" -> Signal.Type.Sigint
        "TERM" -> Signal.Type.Sigterm
        "BREAK" -> Signal.Type.Sigbreak
        else -> return@SignalHandler
    }
    lockListeners()
    try {
        listeners.forEach {
            it(type)
        }
    } finally {
        listenersLock.set(false)
    }
}
