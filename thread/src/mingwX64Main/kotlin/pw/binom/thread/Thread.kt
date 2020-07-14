package pw.binom.thread

import kotlinx.cinterop.*
import platform.windows.*
import pw.binom.atomic.AtomicBoolean

@ThreadLocal
private var privateCurrentThread: Thread? = null

private fun executeInOtherThread(func: () -> Unit): Pair<UInt, HANDLE> {
    return memScoped {
        val thread = alloc<UIntVar>()
        val ptr = StableRef.create(func).asCPointer()
        val handle = CreateThread(null, 0.convert(), staticCFunction<COpaquePointer?, DWORD> {
            initRuntimeIfNeeded()
            val selfPtr = it!!.asStableRef<() -> Unit>()
            val self = selfPtr.get()
            try {
                self()
            } finally {
                selfPtr.dispose()
            }
            0u
        }, ptr, 0.convert(), thread.ptr) ?: throw IllegalStateException("Can't start new Thread")
        thread.value to handle
    }
}

private class MainThread : Thread() {
    override fun start() {
        throw IllegalStateException("Main thread already running")
    }

    override fun run() {
        throw IllegalStateException("Main thread already running")
    }

    override val id: Long = GetCurrentThreadId().toLong()
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
            Sleep(time.toUInt())
        }

        actual fun currentTimeMillis(): Long =
                memScoped {
                    val time = alloc<SYSTEMTIME>()
                    GetSystemTime(time.ptr);
                    (time.wSecond * 1000u) + time.wMilliseconds
                }.toLong()
    }

    actual open fun start() {
        val thread = executeInOtherThread {
            privateCurrentThread = this
            try {
                run()
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                privateCurrentThread = null
            }
        }
        selfId = thread.first
        handle = thread.second
    }

    private var selfId = 0u
    private var handle: HANDLE? = null

    actual open val id: Long
        get() = selfId.toLong()

    protected actual open fun run() {
        runnable?.run()
    }

    actual fun join() {
        WaitForSingleObject(handle ?: return, INFINITE)
    }

    private val interrupted = AtomicBoolean(false)

    actual val isInterrupted: Boolean
        get() = interrupted.value

    actual fun interrupt() {
        interrupted.value=true
    }
}