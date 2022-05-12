package pw.binom.thread

import kotlinx.cinterop.*
import platform.posix.usleep
import platform.windows.*
import kotlin.time.Duration

private var createCount = 0

@ThreadLocal
private var localThread: Thread? = null

private fun genName() = "Thread-${createCount++}"

actual abstract class Thread(var _id: HANDLE?, actual var name: String) {
    actual abstract fun execute()

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
}

private val func: CPointer<CFunction<(COpaquePointer?) -> DWORD>> = staticCFunction { ptr ->
    val thread = ptr!!.asStableRef<Thread>()
    try {
        thread.get().execute()
    } finally {
        thread.dispose()
    }
    0.convert()
}
