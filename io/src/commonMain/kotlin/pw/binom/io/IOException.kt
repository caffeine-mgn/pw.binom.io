package pw.binom.io

expect open class IOException : Exception {
  constructor()
  constructor(message: String?)
  constructor(cause: Throwable?)
  constructor(message: String?, cause: Throwable?)
}
