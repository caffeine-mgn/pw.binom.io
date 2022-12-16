package pw.binom.network

import pw.binom.io.Closeable
import java.nio.channels.SelectionKey
import java.util.concurrent.locks.ReentrantLock

actual interface SelectedEventsOld : Closeable, Iterable<SelectorOld.KeyEvent> {
    var selectedKeys: Set<SelectionKey>
    val lock: ReentrantLock

    actual companion object {
        actual fun create(maxEvents: Int): SelectedEventsOld = SelectedEventsJvm()
    }
}
