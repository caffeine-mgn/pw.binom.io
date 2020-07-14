package pw.binom.atomic

import kotlin.native.concurrent.AtomicLong as NAtomicLong

actual class AtomicLong actual constructor(value: Long) {
    private val atom = NAtomicLong(value)

    actual fun compareAndSet(expected: Long, new: Long): Boolean =
            atom.compareAndSet(expected, new)

    actual fun compareAndSwap(expected: Long, new: Long): Long =
            atom.compareAndSwap(expected, new)

    actual fun addAndGet(delta: Long): Long =
            atom.addAndGet(delta)

    actual fun increment() {
        atom.increment()
    }

    actual fun decrement() {
        atom.decrement()
    }

    actual var value: Long
        get() = atom.value
        set(value) {
            atom.value = value
        }
}