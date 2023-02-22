package pw.binom.thread

import kotlinx.cinterop.*
import platform.common.internal_setThreadName
import platform.common.internal_thread_yield
import platform.posix.*
import kotlin.native.concurrent.Worker
import kotlin.time.Duration

private var createCount = 0

@kotlin.native.concurrent.ThreadLocal
private var localThread: Thread? = null

private fun genName() = "Thread-${createCount++}"

@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(ExperimentalStdlibApi::class)
actual abstract class Thread(var _id: pthread_t, name: String) {

    private var initName = name
    actual var name: String = name
        set(value) {
            internal_setThreadName(_id, value)
            field = value
        }

    actual abstract fun execute()

    actual constructor(name: String) : this(_id = 0.convert(), name = name)

    actual constructor() : this(name = genName())

    internal fun nativeExecute() {
        ThreadMetrics.incThread()
        localThread = this
        try {
            internalIsActive = true
            execute()
        } finally {
            ThreadMetrics.decThread()
            internalIsActive = false
            localThread = null
        }
    }

    actual fun start() {
        if (_id != 0.convert<pthread_t>()) {
            throw IllegalStateException("Thread already started")
        }

        val worker = Worker.start(name = name)
        _id = worker.platformThreadId.convert()
        worker.executeAfter {
            try {
                nativeExecute()
            } finally {
                worker.requestTermination()
            }
        }

//        memScoped {
//            val id = alloc<pthread_tVar>()
//            val ptr = StableRef.create(this@Thread)
//            if (pthread_create(id.ptr, null, func, ptr.asCPointer()) != 0) {
//                ptr.dispose()
//                throw IllegalArgumentException("Can't start thread")
//            }
//            this@Thread._id = id.value
//            this@Thread.name = this@Thread.name
//        }
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

        actual fun yield() {
            internal_thread_yield()
        }
    }

    actual var uncaughtExceptionHandler: UncaughtExceptionHandler = DefaultUncaughtExceptionHandler
    private var internalIsActive = false
    actual val isActive: Boolean
        get() = internalIsActive
}

private val func: CPointer<CFunction<(COpaquePointer?) -> COpaquePointer?>> = staticCFunction { ptr ->
    initRuntimeIfNeeded()
    val thread = ptr!!.asStableRef<Thread>()
    try {
        thread.get().nativeExecute()
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

/*
actual abstract class Thread(var _id: pthread_t, name: String) {

    actual var name: String = name
        set(value) {
            internal_setThreadName(_id, value)
            field = value
        }

    actual abstract fun execute()

    actual constructor(name: String) : this(_id = 0.convert(), name = name)

    actual constructor() : this(name = genName())

    internal fun nativeExecute() {
        ThreadMetrics.incThread()
        try {
            internalIsActive = true
            execute()
        } finally {
            ThreadMetrics.decThread()
            internalIsActive = false
        }
    }

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
            this@Thread.name = this@Thread.name
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
    private var internalIsActive = false
    actual val isActive: Boolean
        get() = internalIsActive
}

private val func: CPointer<CFunction<(COpaquePointer?) -> COpaquePointer?>> = staticCFunction { ptr ->
    initRuntimeIfNeeded()
    val thread = ptr!!.asStableRef<Thread>()
    try {
        thread.get().nativeExecute()
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
*/
