package pw.binom.io.socket

import kotlinx.cinterop.*

abstract class AbstractMutableNetworkAddress : MutableNetworkAddress {
    val data = ByteArray(28)
    var size = 0
        set

    protected var hashCode = 0

    override fun hashCode(): Int = hashCode

    protected fun refreshHashCode(host: String, port: Int) {
        var hashCode = host.hashCode()
        hashCode = 31 * hashCode + port.hashCode()
        this.hashCode = hashCode
    }

    internal inline fun <T> addr(f: MemScope.(CPointer<ByteVar>) -> T): T =
        memScoped {
            this.f(data.refTo(0).getPointer(this))
        }

    override fun toMutable(): MutableNetworkAddress = this

    override fun toString(): String = "$host:$port"
}
