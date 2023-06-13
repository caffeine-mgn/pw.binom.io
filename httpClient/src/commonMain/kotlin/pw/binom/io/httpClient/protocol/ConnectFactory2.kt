package pw.binom.io.httpClient.protocol

import pw.binom.io.AsyncChannel

interface ConnectFactory2 {
    fun createConnect(): HttpConnect
    val supportOverConnection: Boolean
        get() = false

    fun createConnect(channel: AsyncChannel): HttpConnect =
        throw IllegalStateException()
}
