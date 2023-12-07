package pw.binom.io.socket

internal expect fun createNetworkAddress(host: String, port: Int): InetNetworkAddress
internal expect fun createMutableNetworkAddress(): MutableInetNetworkAddress

internal fun throwUnixSocketNotSupported(): Nothing =
  throw RuntimeException("Mingw Target not supports Unix Domain Socket")

internal fun SelectorKey.buildToString() =
  "SelectorKey(flags: ${commonFlagsToString(listenFlags)}, readFlags: $readFlags, isClosed: $isClosed)"

internal fun commonFlagsToString(flags: Int): String {
  if (flags == 0) {
    return "none"
  }
  val sb = StringBuilder()
  if (flags and KeyListenFlags.READ != 0) {
    sb.append("READ ")
  }
  if (flags and KeyListenFlags.WRITE != 0) {
    sb.append("WRITE ")
  }
  if (flags and KeyListenFlags.ERROR != 0) {
    sb.append("ERROR ")
  }
  if (flags and KeyListenFlags.ONCE != 0) {
    sb.append("ONCE ")
  }
  return sb.toString().trim()
}

internal fun Event.buildToString(): String {
  val sb = StringBuilder("Event(flags: ")
    .append(flags.toString())
    .append(", key: ")
    .append(key)
    .append(")")
  return sb.toString()
}
