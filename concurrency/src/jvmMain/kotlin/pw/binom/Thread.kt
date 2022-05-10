package pw.binom

import java.lang.Thread as JvmThread

actual abstract class Thread {
    private val native = JvmThread {
        this@Thread.execute()
    }

    init {
        native.isDaemon = true
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
}
