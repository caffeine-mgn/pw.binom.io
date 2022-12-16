package pw.binom.network

import pw.binom.io.Closeable

expect interface SelectedEventsOld : Closeable, Iterable<SelectorOld.KeyEvent> {
    companion object {
        fun create(maxEvents: Int = 1000): SelectedEventsOld
    }
}
