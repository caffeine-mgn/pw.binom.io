package pw.binom.io.socket

actual interface NetworkChannel : Channel {
    val socket: Socket
    fun regSelector(selector: SocketSelector, key: SocketSelector.SelectorKey)
    fun unregSelector(selector: SocketSelector)
}