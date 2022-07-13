package pw.binom.thread

import kotlin.time.Duration

fun Thread(func: (Thread) -> Unit) = object : Thread() {
    override fun execute() {
        func(this)
    }
}

fun Thread(name: String, func: (Thread) -> Unit) = object : Thread(name) {
    override fun execute() {
        func(this)
    }
}

expect abstract class Thread {
    companion object {
        val currentThread: Thread
        fun sleep(millis: Long)
        fun sleep(duration: Duration)
    }

    constructor()
    constructor(name: String)

    val isActive: Boolean
    var name: String
    val id: Long
    var uncaughtExceptionHandler: UncaughtExceptionHandler
    abstract fun execute()
    fun start()
    fun join()
}
