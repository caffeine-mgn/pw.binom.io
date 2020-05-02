package pw.binom.process

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction
import platform.posix.SIGINT
import platform.posix.SIGTERM
import pw.binom.Platform
import pw.binom.io.Closeable
import pw.binom.thread.FreezedStack

private class SignalListener(val signal: Signal.Type, handler: (Signal.Type) -> Unit) : Closeable {
    private val handler = StableRef.create(handler).asCPointer()
    fun call(signal: Signal.Type) {
        val handlerPtr = handler.asStableRef<(Signal.Type) -> Unit>().get()
        handlerPtr(signal)
    }

    override fun close() {
        handler.asStableRef<(Signal.Type) -> Unit>().dispose()
    }
}

private val signals = FreezedStack<SignalListener>()

private val globalHandler = staticCFunction<Int, Unit> handler@{ signal ->
    val type = Signal.Type.values().find { it.code == signal.toInt() } ?: return@handler
    val signalObj = signals.find { it.signal == type } ?: return@handler
    signalObj.call(type)
    return@handler
}

actual object Signal {
    private const val NOT_SUPPORTED_CODE = -1

    actual enum class Type(val code: Int) {
        CTRL_C(SIGINT),
        CTRL_B(platform.linux.internal_SIGBREAK),
        CLOSE(SIGTERM),
        LOGOFF(NOT_SUPPORTED_CODE),
        SHUTDOWN(NOT_SUPPORTED_CODE)
    }

    actual fun listen(signal: Type, handler: (Type) -> Unit): Closeable {
        if (signal.code == NOT_SUPPORTED_CODE)
            throw IllegalArgumentException("Signal ${signal.name} not supported on Platform $Platform")
        val listener = SignalListener(signal, handler)
        platform.posix.signal(signal.code, globalHandler)
        signals.pushLast(listener)
        return Closeable {
            val it = signals.iterator()
            while (it.hasNext()) {
                val e = it.next()
                if (e === listener) {
                    e.close()
                    it.remove()
                    break
                }
            }
        }
    }

    actual fun closeAll() {
        signals.forEach {
            it.close()
        }
        signals.clear()
    }

}