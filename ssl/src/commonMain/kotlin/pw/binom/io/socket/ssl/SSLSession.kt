package pw.binom.io.socket.ssl

import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable

expect class SSLSession : Closeable {
    enum class State {
        OK, WANT_WRITE, WANT_READ, ERROR, CLOSED
    }

    class Status {
        val state: State
        val bytes: Int
    }

    fun readNet(dst: ByteArray, offset: Int, length: Int): Int
    fun writeNet(dst: ByteArray, offset: Int, length: Int): Int
    fun readNet(dst: ByteBuffer): Int
    fun writeNet(dst: ByteBuffer): Int

    fun writeApp(src: ByteArray, offset: Int, length: Int): Status
    fun readApp(dst: ByteBuffer): Status
    fun writeApp(src: ByteBuffer): Status
}
