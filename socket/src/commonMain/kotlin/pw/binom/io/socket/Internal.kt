package pw.binom.io.socket

//internal expect fun createNetworkAddress(host: String, port: Int): InetNetworkSocketAddress
//internal expect fun createMutableNetworkAddress(): MutableInetNetworkSocketAddress

internal fun throwUnixSocketNotSupported(): Nothing =
  throw RuntimeException("Mingw Target not supports Unix Domain Socket")

internal fun SelectorKey.buildToString() =
  "SelectorKey(flags: ${commonFlagsToString(listenFlags)}, readFlags: $readFlags, isClosed: $isClosed)"

internal fun commonFlagsToString(flags: ListenFlags): String {
  if (flags.isZero) {
    return "none"
  }
  val sb = StringBuilder()
  if (flags.isRead) {
    sb.append("READ ")
  }
  if (flags.isWrite) {
    sb.append("WRITE ")
  }
  if (flags.isError) {
    sb.append("ERROR ")
  }
  if (flags.isOnce) {
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
