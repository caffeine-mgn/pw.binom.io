package pw.binom.network

import pw.binom.io.Closeable
import java.nio.channels.SelectionKey
import java.util.concurrent.locks.ReentrantLock

actual interface SelectedEvents : Closeable, Iterable<Selector.KeyEvent> {
    var selectedKeys: Set<SelectionKey>
    val lock: ReentrantLock

    actual companion object {
        actual fun create(maxEvents: Int): SelectedEvents = SelectedEventsJvm()
    }
}
