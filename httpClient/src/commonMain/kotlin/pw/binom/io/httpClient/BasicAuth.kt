@file:JvmName("BasicAuthClientUtils")

package pw.binom.io.httpClient

import pw.binom.base64.Base64
import pw.binom.io.http.BasicAuth
import kotlin.jvm.JvmName

@Deprecated(message = "Use HttpClient", level = DeprecationLevel.WARNING)
fun AsyncHttpClient.UrlConnect.use(auth: BasicAuth) {
    addHeader("Authorization", auth.headerValue)
}