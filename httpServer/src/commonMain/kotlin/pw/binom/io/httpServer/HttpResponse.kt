package pw.binom.io.httpServer

import pw.binom.AsyncOutput
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.network.CrossThreadKeyHolder

interface HttpResponse {
    var status: Int
    val headers: Map<String, List<String>>
    var enableCompress: Boolean
    fun clearHeaders()
    fun resetHeader(name: String, value: String)
    fun addHeader(name: String, value: String)
    var enableKeepAlive:Boolean
    suspend fun complete(autoFlushSize:UInt= DEFAULT_BUFFER_SIZE.toUInt()):HttpResponseBody
}

interface HttpResponseBody:AsyncOutput{
}