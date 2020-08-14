package pw.binom.thread

import kotlinx.cinterop.*
import platform.posix.*
import pw.binom.atomic.AtomicBoolean
import kotlin.math.round

@ThreadLocal
private var privateCurrentThread: Thread? = null

private fun executeInOtherThread(func: () -> Unit): ULong {
    return memScoped {
        val thread = alloc<ULongVar>()
        val ptr = StableRef.create(func).asCPointer()
        pthread_create(
                thread.ptr.reinterpret(),
                null,
                staticCFunction<COpaquePointer?, Long> {
                    initRuntimeIfNeeded()
                    val selfPtr = it!!.asStableRef<() -> Unit>()
                    val self = selfPtr.get()
                    try {
                        self()
                    } finally {
                        selfPtr.dispose()
                    }
                    return@staticCFunction 0
                } as CPointer<CFunction<(COpaquePointer?) -> COpaquePointer?>>,
                ptr)
        thread.value
    }
}

private class MainThread : Thread() {
    override fun start() {
        throw IllegalStateException("Main thread already running")
    }

    override fun run() {
        throw IllegalStateException("Main thread already running")
    }

    override val id: Long = pthread_self().toLong()
}

actual open class Thread {
    private val runnable: Runnable?

    actual constructor(runnable: Runnable) {
        this.runnable = runnable
    }

    actual constructor() {
        runnable = null
    }

    actual companion object {
        actual val currentThread: Thread
            get() {
                if (privateCurrentThread == null)
                    privateCurrentThread = MainThread()
                return privateCurrentThread!!
            }

        actual fun sleep(time: Long) {
            usleep((time * 1000).toUInt())
        }

        actual fun currentTimeMillis(): Long =
                memScoped {
                    val spec = this.alloc<timespec>()
                    clock_gettime(CLOCK_REALTIME, spec.ptr)

                    var s = spec.tv_sec

                    var ms = round(spec.tv_nsec / 1.0e6).toLong()
                    if (ms > 999) {
                        s++
                        ms = 0
                    }
                    s * 1000 + ms
                }
    }

    actual open fun start() {
        selfId = executeInOtherThread {
            privateCurrentThread = this
            try {
                run()
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                privateCurrentThread = null
            }
        }
    }

    private var selfId: ULong = 0uL

    actual open val id: Long
        get() = selfId.toLong()

    protected actual open fun run() {
        runnable?.run()
    }

    actual fun join() {
        pthread_join(selfId.convert(), null)
    }

    private val interrupted = AtomicBoolean(false)

    actual val isInterrupted: Boolean
        get() = interrupted.value

    actual fun interrupt() {
        interrupted.value = true
    }
}