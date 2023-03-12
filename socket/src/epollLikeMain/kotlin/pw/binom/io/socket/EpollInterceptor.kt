package pw.binom.io.socket

import pw.binom.io.Closeable

expect class EpollInterceptor : Closeable {
    fun wakeup()
    fun interruptWakeup()

    constructor(selector: Selector)
}
