package pw.binom.thread

import kotlin.time.Duration

expect abstract class Thread {
    companion object {
        val currentThread: Thread
        fun sleep(millis: Long)
        fun sleep(duration: Duration)
    }

    constructor()
    constructor(name: String)

    var name: String
    val id: Long
    abstract fun execute()
    fun start()
    fun join()
}
