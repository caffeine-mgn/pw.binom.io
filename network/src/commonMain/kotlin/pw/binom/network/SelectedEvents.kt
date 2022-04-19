package pw.binom.network

import pw.binom.io.Closeable

expect interface SelectedEvents : Closeable, Iterable<Selector.KeyEvent> {
    companion object {
        fun create(maxEvents: Int = 1000): SelectedEvents
    }
}
