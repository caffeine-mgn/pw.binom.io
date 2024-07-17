package pw.binom.io

actual open class EOFException : IOException {
  actual constructor() : super()
  actual constructor(msg: String?) : super(msg)
}
