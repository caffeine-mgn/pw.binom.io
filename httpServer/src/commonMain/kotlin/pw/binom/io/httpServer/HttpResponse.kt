package pw.binom.io.httpServer

import pw.binom.AsyncOutput
import pw.binom.Output
import pw.binom.io.AsyncOutputStream

interface HttpResponse {
    var status: Int
    val headers: Map<String, List<String>>
    var enableCompress:Boolean
    fun clearHeaders()
    fun resetHeader(name: String, value: String)
    fun addHeader(name: String, value: String)
    fun detach(): HttpConnectionState
    suspend fun complete():HttpResponseBody
}

interface HttpResponseBody:AsyncOutput{
}