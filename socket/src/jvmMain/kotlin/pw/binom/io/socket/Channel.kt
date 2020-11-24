package pw.binom.io.socket

import pw.binom.io.Closeable
import java.nio.channels.SelectableChannel
import java.nio.channels.Selector

actual interface Channel : Closeable {
    val selectableChannel: SelectableChannel
    val accepteble: Boolean
}