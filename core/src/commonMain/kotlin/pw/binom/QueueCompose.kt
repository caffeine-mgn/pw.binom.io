package pw.binom

import pw.binom.atomic.AtomicBoolean

class QueueCompose<T, F : T, S : T>(val first: Queue<F>, val second: Queue<S>) : Queue<T> {

    private val turn = AtomicBoolean(true)

    override val isEmpty: Boolean
        get() = first.isEmpty || second.isEmpty

    private val lock = Lock()

    override fun pop(): T = lock.use {
        turn.value = !turn.value

        if (turn.value) {
            if (!first.isEmpty) {
                return@use first.pop()
            }

            turn.value = !turn.value

            if (!second.isEmpty)
                return@use second.pop()

            throw NoSuchElementException()
        } else {
            if (!second.isEmpty) {
                return@use second.pop()
            }

            turn.value = !turn.value

            if (!first.isEmpty)
                return@use first.pop()

            throw NoSuchElementException()
        }
    }
}

operator fun <T, F : T, S : T> Queue<F>.plus(other: Queue<S>) =
        QueueCompose(this, other)