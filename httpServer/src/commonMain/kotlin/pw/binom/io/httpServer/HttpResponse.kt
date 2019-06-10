package pw.binom.io.httpServer

import pw.binom.io.AsyncOutputStream

interface HttpResponse {
    var status: Int
    val output: AsyncOutputStream
    val headers: Map<String, List<String>>
    fun clearHeaders()
    fun resetHeader(name: String, value: String)
    fun addHeader(name: String, value: String)
    fun detach(): HttpConnectionState
    fun disconnect()
}