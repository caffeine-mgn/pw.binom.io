package pw.binom.io.socket

actual interface NetworkChannel : Channel {
    val nsocket: RawSocket
}