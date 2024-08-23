package pw.binom.io.socket

import pw.binom.io.Closeable

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class EpollInterceptor : Closeable {
    fun wakeup()
    fun interruptWakeup()

    constructor(selector: Selector)

  override fun close()
}
