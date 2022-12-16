package pw.binom.network

import kotlinx.cinterop.CArrayPointer
import platform.linux.epoll_event
import pw.binom.io.Closeable

actual interface SelectedEventsOld : Closeable, Iterable<SelectorOld.KeyEvent> {
    val native: CArrayPointer<epoll_event>
    var eventCount: Int
    val maxElements: Int
    var selector: LinuxSelector?

    fun addClosedKey(key: SelectorOld.Key)

    actual companion object {
        actual fun create(maxEvents: Int): SelectedEventsOld = LinuxSelectedEvents(maxEvents)
    }
}
