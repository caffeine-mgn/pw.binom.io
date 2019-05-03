package pw.binom.atomic

import java.util.concurrent.atomic.AtomicReference as JAtomicReference

actual class AtomicReference<T> actual constructor(value: T) {
    private val atom = JAtomicReference(value)

    actual fun compareAndSet(expected: T, new: T): Boolean =
            atom.compareAndSet(expected, new)

    actual fun compareAndSwap(expected: T, new: T): T =
            atom.compareAndExchange(expected, new)

    actual var value: T
        get() = atom.get()
        set(value) {
            atom.set(value)
        }
}