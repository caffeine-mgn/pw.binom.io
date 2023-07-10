package pw.binom.io.httpServer.exceptions

import pw.binom.io.http.HttpException

class MissingQueryArgumentException(val name: String) : HttpException(400) {
  override val message: String
    get() = "Missing query argument \"$name\""
}
