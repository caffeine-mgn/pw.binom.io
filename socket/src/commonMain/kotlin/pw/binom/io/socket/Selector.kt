package pw.binom.io.socket

import pw.binom.io.Closeable
import kotlin.time.Duration

expect class Selector : Closeable {
    constructor()

    fun attach(socket: Socket): SelectorKey
    fun select(timeout: Duration, selectedKeys: SelectedKeys)
    fun wakeup()
}
