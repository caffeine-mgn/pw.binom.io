@file:JvmName("BasicAuthClientUtils")

package pw.binom.io.httpClient

import pw.binom.base64.Base64
import pw.binom.io.http.BasicAuth
import kotlin.jvm.JvmName

fun BasicAuth.set(req: AsyncHttpClient.UrlConnect) {
    req.addHeader("Authorization", "Basic ${Base64.encode("$login:$password".encodeToByteArray())}")
}