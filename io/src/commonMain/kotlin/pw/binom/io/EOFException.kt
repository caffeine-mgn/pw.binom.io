package pw.binom.io

expect open class EOFException : IOException {
  constructor()
  constructor(msg: String?)
}
