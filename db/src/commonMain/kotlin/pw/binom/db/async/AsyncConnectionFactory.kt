package pw.binom.db.async

import pw.binom.url.URI

fun interface AsyncConnectionFactory {
  suspend fun create(url: URI): AsyncConnection
}
