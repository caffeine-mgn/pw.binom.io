package pw.binom.io.http

interface HTTPMethod {
  val code: String

  fun isMatch(method: String) = code.equals(method, ignoreCase = true)

  companion object

  object GET : HTTPMethod {
    override val code: String
      get() = "GET"
  }

  object POST : HTTPMethod {
    override val code: String
      get() = "POST"
  }

  object PUT : HTTPMethod {
    override val code: String
      get() = "PUT"
  }

  object DELETE : HTTPMethod {
    override val code: String
      get() = "DELETE"
  }

  object MKCOL : HTTPMethod {
    override val code: String
      get() = "MKCOL"
  }

  object COPY : HTTPMethod {
    override val code: String
      get() = "COPY"
  }

  object PROPPATCH : HTTPMethod {
    override val code: String
      get() = "PROPPATCH"
  }

  object LOCK : HTTPMethod {
    override val code: String
      get() = "LOCK"
  }

  object UNLOCK : HTTPMethod {
    override val code: String
      get() = "UNLOCK"
  }

  object ORDERPATCH : HTTPMethod {
    override val code: String
      get() = "ORDERPATCH"
  }

  object MOVE : HTTPMethod {
    override val code: String
      get() = "MOVE"
  }

  object PROPFIND : HTTPMethod {
    override val code: String
      get() = "PROPFIND"
  }

  object HEAD : HTTPMethod {
    override val code: String
      get() = "HEAD"
  }

  object TRACE : HTTPMethod {
    override val code: String
      get() = "TRACE"
  }

  object CONNECT : HTTPMethod {
    override val code: String
      get() = "CONNECT"
  }

  object PATCH : HTTPMethod {
    override val code: String
      get() = "PATCH"
  }

  object OPTIONS : HTTPMethod {
    override val code: String
      get() = "OPTIONS"
  }
}
