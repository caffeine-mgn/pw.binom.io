@file:JvmName("LockUtilsKt")

package pw.binom.concurrency

import kotlin.jvm.JvmName
import kotlin.time.Duration

expect class ReentrantLock : Lock {
    constructor()

    override fun lock()
    override fun tryLock(): Boolean
    override fun unlock()

    fun newCondition(): Condition

    class Condition {
        fun await()

        /**
         * Stops the current thread until it receives an event. Event can send call of [signal] or [signalAll]
         *
         * @return [false] if the waiting time detectably elapsed before return from the method, else [true]
         */
        fun await(duration: Duration): Boolean
        fun signal()
        fun signalAll()
    }
}
