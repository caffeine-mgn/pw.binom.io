package pw.binom.io.httpClient

import pw.binom.io.AsyncCloseable
import pw.binom.io.http.HttpInput

interface HttpResponse : AsyncCloseable, HttpInput {
  val responseCode: Int
}
