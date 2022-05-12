package pw.binom.thread

import kotlin.time.Duration
import java.lang.Thread as JvmThread

private var createCount = 0
private val localThread = ThreadLocal<Thread>()

actual abstract class Thread constructor(val native: JvmThread) {
    actual constructor(name: String) : this(
        native = BinonJvmThread(name),
    )

    actual constructor() : this("Thread-${createCount++}")

    private class BinonJvmThread(name: String) : JvmThread(name) {
        lateinit var binomThread: Thread

        init {
            isDaemon = true
        }

        override fun run() {
            binomThread.execute()
        }
    }

    init {
        (native as? BinonJvmThread)?.binomThread = this
    }

    actual abstract fun execute()

    actual fun start() {
        native.start()
    }

    actual val id: Long
        get() = native.id

    actual fun join() {
        native.join()
    }

    actual var name: String
        get() = native.name
        set(value) {
            native.name = value
        }

    actual companion object {
        actual val currentThread: Thread
            get() {
                val l = localThread.get()
                if (l != null) {
                    return l
                }

                val thread = JvmThread.currentThread()
                if (thread !is BinonJvmThread) {
                    val wrapper = object : Thread(thread) {
                        override fun execute() {
                            thread.run()
                        }
                    }
                    localThread.set(wrapper)
                    return wrapper
                }
                return thread.binomThread
            }

        actual fun sleep(millis: Long) {
            JvmThread.sleep(millis)
        }

        actual fun sleep(duration: Duration) {
            JvmThread.sleep(duration.inWholeMilliseconds)
        }
    }
}
