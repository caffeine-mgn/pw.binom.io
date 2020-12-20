package pw.binom.io.socket.nio

import pw.binom.doFreeze
import pw.binom.io.socket.ServerSocketChannel
import pw.binom.io.socket.SocketSelector

class TcpConnectionServer(
    val manager: SocketNIOManager,
    val server: ServerSocketChannel,
    val handler: ConnectHandler
) : AbstractConnection() {
    lateinit var key: SocketSelector.SelectorKey

    override fun readyForWrite(): Boolean {
        TODO("Not yet implemented")
    }

    override fun readyForRead(): Boolean {
        val cl = server.accept() ?: return true
        val holder = SocketHolder(cl)
        val connection = TcpConnectionRaw(attachment = null, holder = holder)
        cl.blocking = false
        val selectionKey = manager.selector.reg(cl, connection)
        holder.selectionKey = selectionKey
        holder.doFreeze()
        this.handler.connected(connection)
        return true
    }

    override fun close() {
        key.cancel()
        server.close()
    }

}