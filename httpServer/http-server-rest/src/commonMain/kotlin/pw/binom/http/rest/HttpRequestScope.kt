package pw.binom.http.rest

import pw.binom.date.DateTime
import kotlin.time.Duration

interface HttpRequestScope {
  enum class SameSite {
    STRICT,
    LAX,
    NONE,
  }

  fun setCookie(
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

  fun getCookie(name: String): String?
}
