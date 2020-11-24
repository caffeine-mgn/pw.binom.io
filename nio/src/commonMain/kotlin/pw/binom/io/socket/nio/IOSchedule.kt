package pw.binom.io.socket.nio

import pw.binom.ByteBuffer
import kotlin.coroutines.Continuation

abstract class IOSchedule(val buffer: ByteBuffer) {
    abstract fun finish(value: Result<Int>)
}

class IOScheduleContinuation(buffer: ByteBuffer, val con: Continuation<Int>) : IOSchedule(buffer) {
    override fun finish(value: Result<Int>) {
        con.resumeWith(value)
    }
}