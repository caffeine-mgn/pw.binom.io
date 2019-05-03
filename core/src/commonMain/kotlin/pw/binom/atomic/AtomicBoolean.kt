package pw.binom.atomic

class AtomicBoolean(value: Boolean) {
    private val atom = AtomicInt(boolToInt(value))
    fun compareAndSet(expected: Boolean, new: Boolean): Boolean =
            atom.compareAndSet(boolToInt(expected), boolToInt(new))

    private fun boolToInt(value: Boolean) = if (value) 1 else 0

    var value: Boolean
        get() = atom.value == 1
        set(value: Boolean) {
            atom.value = boolToInt(value)
        }
}