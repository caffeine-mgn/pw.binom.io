package pw.binom.thread

import java.util.concurrent.atomic.AtomicBoolean
import java.lang.Thread as JThread

private var privateCurrentThread = ThreadLocal<Thread>()

private fun executeInOtherThread(func: () -> Unit): JThread {
    val thread = JThread {
        func()
    }
    thread.start()
    return thread
}

private class MainThread : Thread() {
    override fun start() {
        throw IllegalStateException("Main thread already running")
    }

    override fun run() {
        throw IllegalStateException("Main thread already running")
    }

    override val id: Long = JThread.currentThread().id
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
                if (privateCurrentThread.get() == null)
                    privateCurrentThread.set(MainThread())
                return privateCurrentThread.get()
            }

        actual fun sleep(time: Long) {
            JThread.sleep(time)
        }

        actual fun currentTimeMillis(): Long =
                System.currentTimeMillis()
    }

    actual open fun start() {
        selfThread = executeInOtherThread {
            privateCurrentThread.set(this)
            try {
                run()
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                privateCurrentThread.remove()
            }
        }
    }

    private var selfThread: JThread? = null

    actual open val id: Long
        get() = selfThread!!.id

    protected actual open fun run() {
        runnable?.run()
    }

    actual fun join() {
        selfThread?.join()
    }

    private val interrupted = AtomicBoolean(false)

    actual val isInterrupted: Boolean
        get() = interrupted.get()

    actual fun interrupt() {
        interrupted.set(true)
    }
}