package pw.binom.network

import kotlinx.cinterop.CArrayPointer
import platform.linux.epoll_event
import pw.binom.io.Closeable

actual interface SelectedEvents : Closeable, Iterable<Selector.KeyEvent> {
    val native: CArrayPointer<epoll_event>
    var eventCount: Int
    val maxElements: Int
    var selector: MingwSelector?

    actual companion object {
        actual fun create(maxEvents: Int): SelectedEvents = MingwSelectedEvents(maxEvents)
    }
}
