package pw.binom.thread

import kotlinx.cinterop.*
import platform.posix.*
import kotlin.time.Duration

private var createCount = 0

@ThreadLocal
private var localThread: Thread? = null

private fun genName() = "Thread-${createCount++}"

actual abstract class Thread(var _id: pthread_t, actual var name: String) {

    actual abstract fun execute()

    actual constructor(name: String) : this(_id = 0.convert(), name = name)

    actual constructor() : this(name = genName())

    actual fun start() {
        if (_id != 0.convert<pthread_t>()) {
            throw IllegalStateException("Thread already started")
        }
        memScoped {
            val id = alloc<pthread_tVar>()
            val ptr = StableRef.create(this@Thread)
            if (pthread_create(id.ptr, null, func, ptr.asCPointer()) != 0) {
                ptr.dispose()
                throw IllegalArgumentException("Can't start thread")
            }
            this@Thread._id = id.value
        }
    }

    actual val id: Long
        get() = _id.toLong()

    actual fun join() {
        pthread_join(_id, null)
    }

    actual companion object {
        actual val currentThread: Thread
            get() {
                val thread = localThread
                if (thread != null) {
                    return thread
                }
                val wrapper = object : Thread(
                    _id = pthread_self(),
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
}

private val func: CPointer<CFunction<(COpaquePointer?) -> COpaquePointer?>> = staticCFunction { ptr ->
    val thread = ptr!!.asStableRef<Thread>()
    try {
        thread.get().execute()
    } catch (e: Throwable) {
        thread.get().uncaughtExceptionHandler.uncaughtException(
            thread = thread.get(),
            throwable = e,
        )
    } finally {
        thread.dispose()
    }
    null
}
