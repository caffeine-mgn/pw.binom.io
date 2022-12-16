package pw.binom.io.socket

abstract class AbstractMutableNetworkAddress : MutableNetworkAddress {
    val data = ByteArray(28)
    var size = 0
        internal set
}
