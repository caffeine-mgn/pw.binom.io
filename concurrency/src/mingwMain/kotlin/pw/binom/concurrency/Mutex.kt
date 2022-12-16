package pw.binom.concurrency

import kotlinx.cinterop.convert
import kotlinx.cinterop.invoke
import platform.windows.*
import kotlin.time.Duration

value class Mutex(val handler: HANDLE) {
    companion object {
        fun create(locked: Boolean) {
            Mutex(CreateMutex!!.invoke(null, if (locked) 1 else 0, null)!!)
        }
    }

    fun lock(dd: Duration): Boolean {
        val lockDuration = if (dd.isInfinite())
            INFINITE
        else
            dd.inWholeMilliseconds.toUInt()
        dd.inWholeMilliseconds
        return when (val out = WaitForSingleObject(handler, lockDuration)) {
            WAIT_FAILED -> throw RuntimeException("Can't block Mutex. Error #${GetLastError()}")
            WAIT_ABANDONED, WAIT_OBJECT_0 -> true
            WAIT_TIMEOUT.convert<UInt>() -> false
            else -> throw RuntimeException("Unknown Mutex Lock State. $out")
        }
    }

    fun unlock() {
        if (ReleaseMutex(handler) == 0) {
            throw RuntimeException("Can't Release Mutex. Error #${GetLastError()}")
        }
    }

    fun close() {
        CloseHandle(handler)
    }
}
