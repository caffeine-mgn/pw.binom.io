package pw.binom.concurrency

import kotlinx.cinterop.convert
import kotlinx.cinterop.invoke
import platform.windows.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

inline class Mutex(val handler: HANDLE) {
    companion object {
        fun create(locked: Boolean) {
            Mutex(CreateMutex!!.invoke(null, if (locked) 1 else 0, null)!!)
        }
    }

    @OptIn(ExperimentalTime::class)
    fun lock(dd: Duration): Boolean {
        val lockDuration = if (dd.isInfinite())
            INFINITE
        else
            dd.toLongMilliseconds().toUInt()
        dd.toLongMilliseconds()
        return when (val out = WaitForSingleObject(handler, lockDuration)) {
            WAIT_FAILED -> throw RuntimeException("Can't block Mutex. Error #${GetLastError()}")
            WAIT_ABANDONED, WAIT_OBJECT_0 -> true
            WAIT_TIMEOUT.convert<UInt>() -> false
            else -> throw RuntimeException("Unknown Mutex Lock State. $out")
        }
    }

    fun unlock(){
        if (ReleaseMutex(handler)==0){
            throw RuntimeException("Can't Release Mutex. Error #${GetLastError()}")
        }
    }

    fun close(){
        CloseHandle(handler)
    }
}