package pw.binom.concurrency

import kotlin.time.Duration

interface LockWithTimeout : Lock {
    fun lock(timeout: Duration): Boolean
}
