package pw.binom.io.httpServer

import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import pw.binom.io.http.*
import pw.binom.io.httpServer.exceptions.MissingQueryArgumentException
import pw.binom.url.Path
import pw.binom.url.PathMask
import pw.binom.url.URI

interface HttpServerExchange {
  val requestURI: URI
  val input: AsyncInput
  val output: AsyncOutput
  val requestHeaders: Headers
  val requestMethod: String
  val responseStarted: Boolean
  val requestContext: Path

  fun getQueryParams() = requestURI.query?.toMap() ?: emptyMap()
  fun getPathVariables(mask: PathMask): Map<String, String> = requestURI.path.getVariables(mask) ?: emptyMap()
  fun getPathVariables(mask: String): Map<String, String> = requestURI.path.getVariables(mask) ?: emptyMap()
  fun isQueryParamExist(name: String): Boolean = getQueryParams().containsKey(name)
  fun response() = DefaultHttpServerResponse(this)

  /**
   * Returns GET param by [name]. If param is missing will throw [MissingQueryArgumentException].
   * Call [isQueryParamExist] for get param [name] exist
   * @throws MissingQueryArgumentException when param [name] missing
   */
  @Throws(MissingQueryArgumentException::class)
  fun getQueryParam(name: String): String? {
    val params = getQueryParams()
    if (!params.containsKey(name)) {
      throw MissingQueryArgumentException("name")
    }
    return params[name]
  }

  suspend fun startResponse(statusCode: Int) {
    startResponse(
      statusCode = statusCode,
      headers = EmptyHeaders,
    )
  }

  suspend fun startResponse(statusCode: Int, vararg headers: Pair<String, String>) {
    startResponse(
      statusCode = statusCode,
      headers = headers.toHeaders(),
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
