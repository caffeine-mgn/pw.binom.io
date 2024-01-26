@file:Suppress("ktlint:standard:no-wildcard-imports")

package pw.binom.thread

import kotlinx.cinterop.*
import platform.common.*
import platform.posix.*
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner
import kotlin.time.Duration

private var createCount = 0

@kotlin.native.concurrent.ThreadLocal
private var localThread: Thread? = null

private fun genName() = "Thread-${createCount++}"

@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(UnsafeNumber::class, ExperimentalForeignApi::class)
actual abstract class Thread(_id: Long, name: String) {
  private var threadDataPtr = nativeHeap.alloc<ThreadData>().ptr // malloc(sizeOf<ThreadData>().convert())!!.reinterpret<ThreadData>()

  //  private val e = internal_createThreadData()!!
  var _id: Long
    get() = internal_get_thread_id(threadDataPtr)
    set(value) {
      internal_set_thread_id(threadDataPtr, value)
    }

  @OptIn(ExperimentalNativeApi::class)
  private val cleaner =
    createCleaner(threadDataPtr) {
      nativeHeap.free(it)
    }

  //    private var initName = name
  actual var name: String = name
    set(value) {
      val f = _id
      if (f != 0L) {
        internal_setThreadName2(threadDataPtr, value)
      }
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
    if (_id != 0L) {
      throw IllegalStateException("Thread already started")
    }

//        val worker = Worker.start(name = name)
//        val currentId = worker.platformThreadId
//        _id = currentId.convert()
//        worker.executeAfter {
//            try {
//                nativeExecute()
//            } finally {
//                println("Try to free thread. self=${pthread_self()}...")
//                threadCleanup.executeAfter(1.seconds.inWholeMicroseconds) {
// //                    val nn = pthread_kill(currentId.convert(), SIGUSR1)
//                    val bb = worker.requestTermination().result
//                    println("--Thread finished! self=${pthread_self()} ${Worker.activeWorkers.size}")
//                }
//            }
//        }

    memScoped {
//            val id = alloc<pthread_tVar>()
      val ptr = StableRef.create(this@Thread)
      if (internal_pthread_create(threadDataPtr, null, func, ptr.asCPointer()) != 0) {
        ptr.dispose()
        throw IllegalArgumentException("Can't start thread $errno")
      }
//            this@Thread._id = id.value
      this@Thread.name = this@Thread.name
    }
  }

  actual val id: Long
    get() = _id

  actual fun join() {
    if (internalIsActive) {
      internal_pthread_join(threadDataPtr)
    }
//        pthread_join(_id.reinterpret(), null)
  }

  init {
    this._id = _id
    this.name = name
  }

  actual companion object {
    actual val currentThread: Thread
      get() {
        val thread = localThread
        if (thread != null) {
          return thread
        }
        val wrapper =
          object : Thread(
            _id = internal_pthread_self(), // pthread_self().reinterpret<internal_pthread_t>(),
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

@OptIn(ExperimentalForeignApi::class)
private val func: CPointer<CFunction<(COpaquePointer?) -> COpaquePointer?>> =
  staticCFunction { ptr ->
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
