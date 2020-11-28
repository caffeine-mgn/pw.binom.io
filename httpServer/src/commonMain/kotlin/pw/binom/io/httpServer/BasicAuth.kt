@file:JvmName("BasicAuthServerUtils")

package pw.binom.io.httpServer

import pw.binom.base64.Base64
import pw.binom.io.http.BasicAuth
import pw.binom.io.httpClient.AsyncHttpClient
import kotlin.jvm.JvmName

fun BasicAuth.Companion.set(req: AsyncHttpClient.UrlConnect): BasicAuth? {
    val authorization = req.headers["Authorization"]?.singleOrNull() ?: return null
    if (!authorization.startsWith("Basic "))
        return null
    val sec = Base64.decode(authorization.removePrefix("Basic ")).decodeToString()
    val items = sec.split(':', limit = 2)
    return BasicAuth(login = items[0], password = items[1])
}