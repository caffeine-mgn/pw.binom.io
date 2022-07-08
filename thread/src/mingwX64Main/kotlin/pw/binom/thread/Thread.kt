package pw.binom.thread

import kotlinx.cinterop.*
import platform.posix.pthread_setname_np
import platform.posix.usleep
import platform.windows.*
import kotlin.time.Duration

private var createCount = 0

@kotlin.native.concurrent.ThreadLocal
private var localThread: Thread? = null

private fun genName() = "Thread-${createCount++}"

actual abstract class Thread(var _id: HANDLE?, name: String) {
    actual abstract fun execute()
    internal fun nativeExecute() {
        try {
            internalIsActiove = true
            execute()
        } finally {
            internalIsActiove = false
        }
    }

    actual var name: String = name
        set(value) {
            if (_id != null) {
                pthread_setname_np(id.convert(), value)
            }
            field = value
        }

    actual constructor(name: String) : this(_id = null, name = name)

    actual constructor() : this(name = genName())

    actual fun start() {
        if (_id != null) {
            throw IllegalStateException("Thread already started")
        }
        val ptr = StableRef.create(this@Thread)
        val id2 = CreateThread(null, 0, func, ptr.asCPointer(), 0, null)
        if (id2 == null) {
            ptr.dispose()
            throw IllegalArgumentException("Can't start thread")
        }
        this@Thread._id = id2
        this.name = this.name
    }

    actual val id: Long
        get() = GetThreadId(_id).toLong()

    actual fun join() {
        WaitForSingleObject(_id, INFINITE)
    }

    actual companion object {
        actual val currentThread: Thread
            get() {
                val thread = localThread
                if (thread != null) {
                    return thread
                }
                val wrapper = object : Thread(
                    _id = GetCurrentThread(),
                    name = genName(),
                ) {
                    override fun execute() {
                        throw IllegalStateException()
                    }
                }
                localThread = wrapper
                return wrapper
            }

        actual fun sleep(millis: Long) {
            usleep((millis * 1000).toUInt())
        }

        actual fun sleep(duration: Duration) {
            sleep(duration.inWholeMilliseconds)
        }
    }

    actual var uncaughtExceptionHandler: UncaughtExceptionHandler = DefaultUncaughtExceptionHandler
    private var internalIsActiove = false
    actual val isActive: Boolean
        get() = internalIsActiove
}

private val func: CPointer<CFunction<(COpaquePointer?) -> DWORD>> = staticCFunction { ptr ->
    val thread = ptr!!.asStableRef<Thread>()
    try {
        thread.get().nativeExecute()
    } catch (e: Throwable) {
        thread.get().uncaughtExceptionHandler.uncaughtException(
            thread = thread.get(),
            throwable = e
        )
    } finally {
        thread.dispose()
    }
    0.convert()
}
