package pw.binom.process

import pw.binom.io.Closeable
import sun.misc.SignalHandler
import java.util.concurrent.CopyOnWriteArrayList
import sun.misc.Signal as JSignal

private val globalHandler = SignalHandler { sig ->
    val type = Signal.Type.values().find { it.code == sig.name } ?: return@SignalHandler
    signals.forEach {
        if (it.signal == type) {
            it.call(type)
        }
    }
}

private class SignalListener(val signal: Signal.Type, val handler: (Signal.Type) -> Unit) : Closeable {
    fun call(signal: Signal.Type) {
        handler(signal)
    }

    override fun close() {

    }
}

private val signals = CopyOnWriteArrayList<SignalListener>()
private val handeled = HashSet<Signal.Type>()

actual object Signal {
    private const val NOT_SUPPORTED_CODE = ""

    actual enum class Type(val code: String) {
        CTRL_C("INT"),
        CTRL_B(NOT_SUPPORTED_CODE),
        CLOSE("TERM"),
        LOGOFF(NOT_SUPPORTED_CODE),
        SHUTDOWN(NOT_SUPPORTED_CODE)
    }

    actual fun listen(signal: Type, handler: (Type) -> Unit): Closeable {
        val listener = SignalListener(signal, handler)
        signals += listener
        if (signal !in handeled) {
            JSignal.handle(JSignal(signal.code), globalHandler)
        }
        return Closeable {
            signals -= listener
        }
    }

    actual fun closeAll() {
        signals.clear()
    }

    actual fun addShutdownHook(func: () -> Unit) {
        Runtime.getRuntime().addShutdownHook(Thread(Runnable {
            func()
        }))
    }

}