package pw.binom.io.httpServer

import pw.binom.date.Date
import pw.binom.io.AsyncChannel
import pw.binom.io.http.AsyncAsciiChannel

class ServerAsyncAsciiChannel(channel: AsyncChannel) : AsyncAsciiChannel(channel) {
    var lastActive = Date.now
        private set

    fun activeUpdate() {
        lastActive = Date.now
    }
}