package pw.binom.io.socket.ssl

expect class SSLSession {
    enum class State {
        OK, WANT_WRITE, WANT_READ, ERROR
    }

    class Status{
        val state:State
        val bytes:Int
    }

    fun readNet(dst: ByteArray, offset: Int, length: Int): Int
    fun writeNet(dst: ByteArray, offset: Int, length: Int): Int
    fun writeApp(src: ByteArray, offset: Int, length: Int): Status
    fun readApp(dst: ByteArray, offset: Int, length: Int): Status
}