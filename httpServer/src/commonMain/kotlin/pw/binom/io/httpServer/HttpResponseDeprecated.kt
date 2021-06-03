package pw.binom.io.httpServer

import pw.binom.AsyncOutput
import pw.binom.DEFAULT_BUFFER_SIZE

@Deprecated(message = "Will be removed")
interface HttpResponseDeprecated {
    var status: Int
    val headers: Map<String, List<String>>
    var enableCompress: Boolean
    fun clearHeaders()
    fun resetHeader(name: String, value: String)
    fun addHeader(name: String, value: String)
    var enableKeepAlive:Boolean
    suspend fun complete(autoFlushSize: Int = DEFAULT_BUFFER_SIZE): HttpResponseBodyDeprecated
}

@Deprecated(message = "Will be removed")
interface HttpResponseBodyDeprecated:AsyncOutput{
}