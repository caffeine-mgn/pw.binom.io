package pw.binom.http.rest

import pw.binom.date.DateTime
import pw.binom.io.http.Headers
import pw.binom.io.http.headersOf
import kotlin.time.Duration

interface HttpRequestScope {
  enum class SameSite {
    STRICT,
    LAX,
    NONE,
  }

  fun setResponseCookie(
    key: String,
    value: String,
    expires: DateTime? = null,
    maxAge: Duration? = null,
    domain: String? = null,
    path: String? = null,
    secure: Boolean = false,
    httpOnly: Boolean = false,
    sameSite: SameSite? = null,
  )

  fun getRequestCookie(name: String): String?
  fun getRequestHeader(name: String): List<String>
  fun addResponseHeader(name: String, value: String) = addResponseHeader(headersOf(name to value))
  fun addResponseHeader(headers: Headers)
}
