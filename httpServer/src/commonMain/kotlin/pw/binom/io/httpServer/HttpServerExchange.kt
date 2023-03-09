package pw.binom.io.httpServer

import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import pw.binom.io.http.EmptyHeaders
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.Headers
import pw.binom.io.http.emptyHeaders
import pw.binom.url.PathMask
import pw.binom.url.URI

interface HttpServerExchange {
    val requestURI: URI
    val input: AsyncInput
    val output: AsyncOutput
    val requestHeaders: Headers
    val requestMethod: String
    val responseStarted: Boolean

    fun getQueryParams() = requestURI.query?.toMap() ?: emptyMap()
    fun getPathVariables(mask: PathMask): Map<String, String> = requestURI.path.getVariables(mask) ?: emptyMap()
    suspend fun startResponse(statusCode: Int) {
        startResponse(
            statusCode = statusCode,
            headers = EmptyHeaders,
        )
    }

    suspend fun startResponse(statusCode: Int, vararg headers: Pair<String, String>) {
        val map = if (headers.isEmpty()) {
            emptyHeaders()
        } else {
            val map = HashHeaders2()
            headers.forEach { (key, value) ->
                map.add(key = key, value = value)
            }
            map
        }
        startResponse(
            statusCode = statusCode,
            headers = map,
        )
    }

//    suspend fun startResponse(statusCode: Int, headers: Map<String, String>) {
//        val map = HashHeaders2()
//        headers.forEach { (key, value) ->
//            map.add(key = key, value = value)
//        }
//        startResponse(
//            statusCode = statusCode,
//            headers = map,
//        )
//    }

    suspend fun startResponse(statusCode: Int, headers: Map<String, List<String>>) {
        val map = if (headers.isEmpty()) {
            emptyHeaders()
        } else {
            val map = HashHeaders2()
            headers.forEach { (key, value) ->
                map.add(key = key, value = value)
            }
            map
        }
        startResponse(
            statusCode = statusCode,
            headers = map,
        )
    }

    suspend fun startResponse(statusCode: Int, headers: Headers)
}
