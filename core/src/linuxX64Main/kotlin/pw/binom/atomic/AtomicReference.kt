package pw.binom.atomic

import kotlin.native.concurrent.AtomicReference as NAtomicReference

actual class AtomicReference<T> actual constructor(value: T) {
    private val atom = NAtomicReference(value)

    actual fun compareAndSet(expected: T, new: T): Boolean =
            atom.compareAndSet(expected, new)

    actual fun compareAndSwap(expected: T, new: T): T =
            atom.compareAndSwap(expected, new)

    actual var value: T
        get() = atom.value
        set(value) {
            atom.value = value
        }
}