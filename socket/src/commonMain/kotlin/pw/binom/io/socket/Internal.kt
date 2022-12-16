package pw.binom.io.socket

internal expect fun createNetworkAddress(host: String, port: Int): NetworkAddress
internal expect fun createMutableNetworkAddress(): MutableNetworkAddress

internal fun throwUnixSocketNotSupported(): Nothing =
    throw RuntimeException("Mingw Target not supports Unix Domain Socket")

internal fun SelectorKey.buildToString() =
    "Selector(flags: ${commonFlagsToString(listenFlags)}, attachment: $attachment)"

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
    return sb.toString().trim()
}

internal fun Event.buildToString(): String {
    val sb = StringBuilder("Event(flags: ${flags.toString(2).padStart(4, '0')} ")
    sb.append(commonFlagsToString(flags))
    sb.append("key: ").append(key).append(")")
    return sb.toString()
}
