@file:JvmName("BasicAuthServerUtils")

package pw.binom.io.httpServer

import pw.binom.base64.Base64
import pw.binom.io.http.BasicAuth
import pw.binom.io.httpClient.AsyncHttpClient
import kotlin.jvm.JvmName

val HttpRequest.basicAuth: BasicAuth?
    get() {
        val authorization = headers["Authorization"]?.singleOrNull() ?: return null
        if (!authorization.startsWith("Basic "))
            return null
        val sec = Base64.decode(authorization.removePrefix("Basic ")).decodeToString()
        val items = sec.split(':', limit = 2)
        return BasicAuth(login = items[0], password = items[1])
    }

/**
 * Adds header "WWW-Authenticate" and set status 401
 */
fun HttpResponse.requestBasicAuth(realm: String? = null, service: String? = null) {
    val sb = StringBuilder("Basic")
    if (realm != null) {
        sb.append(" realm=\"").append(realm).append("\"")
    }
    if (service != null) {
        if (realm != null) {
            sb.append(",")
        }
        sb.append("service=\"").append(service).append("\"")
    }
    resetHeader("WWW-Authenticate", "Basic")
    status = 401
}