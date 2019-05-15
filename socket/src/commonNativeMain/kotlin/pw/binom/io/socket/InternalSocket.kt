package pw.binom.io.socket


internal expect class NativeSocketHolder{
    val code:Int
}

internal expect class NativeEpoll {
    constructor(connectionCount: Int)

    fun add(socket: NativeSocketHolder)
    fun remove(socket: NativeSocketHolder)
    fun free()
    fun wait(list: NativeEpollList, connectionCount: Int, timeout: Int): Int
    fun edit(socket: NativeSocketHolder, readFlag:Boolean,writeFlag:Boolean)
}

expect class NativeEvent

internal expect val NativeEvent.isClosed:Boolean
internal expect val NativeEvent.isReadable:Boolean
internal expect val NativeEvent.isWritable:Boolean
internal expect val NativeEvent.socId:Int
internal expect class NativeEpollList {
    constructor(connectionCount: Int)

    operator fun get(index: Int): NativeEvent
    fun free()
}

internal expect fun setBlocking(native: NativeSocketHolder, value: Boolean)
internal expect fun closeSocket(native: NativeSocketHolder)
internal expect fun initNativeSocket(): NativeSocketHolder
internal expect fun recvSocket(socket: NativeSocketHolder, data: ByteArray, offset: Int, length: Int): Int
internal expect fun bindSocket(socket: NativeSocketHolder, host:String, port: Int)
internal expect fun connectSocket(native: NativeSocketHolder, host: String, port: Int)
internal expect fun sendSocket(socket: NativeSocketHolder, data: ByteArray, offset: Int, length: Int)
internal expect fun acceptSocket(socket: NativeSocketHolder): NativeSocketHolder