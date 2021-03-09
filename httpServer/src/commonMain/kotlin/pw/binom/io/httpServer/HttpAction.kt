package pw.binom.io.httpServer

import pw.binom.io.http.Headers
import pw.binom.io.http.MutableHeaders

interface HttpAction {
    val requestHeaders: Headers
    val responseHeaders: MutableHeaders
    val requestUri: String
    var responseCode: Int
    fun readBinary()
    fun readText()
    fun writeBinary()
    fun writeText()

    enum class Status {
        READY_FOR_READ,
        READING,

    }
}