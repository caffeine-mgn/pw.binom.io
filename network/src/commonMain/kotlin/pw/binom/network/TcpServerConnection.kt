package pw.binom.network

class TcpServerConnection(val dispatcher: NetworkDispatcher, val channel: TcpServerSocketChannel) :
    AbstractConnection() {

    lateinit var key:Selector.Key

    override fun readyForWrite(): Boolean {
        TODO("Not yet implemented")
    }

    override fun connected() {
        TODO("Not yet implemented")
    }

    override fun error() {
        TODO("Not yet implemented")
    }

    override fun readyForRead(): Boolean {
        TODO("Not yet implemented")
    }

    override fun close() {
        channel.close()
    }

    fun accept(address: NetworkAddress.Mutable? = null): TcpConnection? {
        val newChannel = channel.accept(null) ?: return null
        return dispatcher.attach(newChannel)
    }
}