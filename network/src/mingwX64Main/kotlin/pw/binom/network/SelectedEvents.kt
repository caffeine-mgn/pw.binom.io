package pw.binom.network

import pw.binom.io.Closeable

actual interface SelectedEvents : Closeable, Iterable<Selector.KeyEvent> {
    actual companion object {
        actual fun create(maxEvents: Int): SelectedEvents = MingwSelectedEvents(maxEvents)
    }
}
