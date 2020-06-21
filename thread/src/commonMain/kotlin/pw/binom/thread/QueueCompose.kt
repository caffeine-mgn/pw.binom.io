package pw.binom.thread

import pw.binom.PopResult
import pw.binom.Queue
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.hold

@Suppress("UNCHECKED_CAST")
class QueueCompose<T, F : T, S : T>(val first: Queue<F>, val second: Queue<S>) : Queue<T> {
    override val size: Int
        get() = lock.hold {
            first.size + second.size
        }

    private val turn = AtomicBoolean(true)

    override val isEmpty: Boolean
        get() = first.isEmpty || second.isEmpty

    private val lock = Lock()

    override fun pop(): T = lock.hold {
        turn.value = !turn.value

        if (turn.value) {
            if (!first.isEmpty) {
                return@hold first.pop()
            }

            turn.value = !turn.value

            if (!second.isEmpty)
                return@hold second.pop()

            throw NoSuchElementException()
        } else {
            if (!second.isEmpty) {
                return@hold second.pop()
            }

            turn.value = !turn.value

            if (!first.isEmpty)
                return@hold first.pop()

            throw NoSuchElementException()
        }
    }

    override fun pop(dist: PopResult<T>) {
        lock.hold {
            turn.value = !turn.value

            if (turn.value) {
                first.pop(dist as PopResult<F>)
                if (!dist.isEmpty) {
                    return@hold
                }

                turn.value = !turn.value
                second.pop(dist as PopResult<S>)

                if (!dist.isEmpty)
                    return@hold
            } else {
                second.pop(dist as PopResult<S>)
                if (!dist.isEmpty) {
                    return@hold
                }

                turn.value = !turn.value

                first.pop(dist as PopResult<F>)
                if (!dist.isEmpty)
                    return@hold
            }
        }
    }
}

operator fun <T, F : T, S : T> Queue<F>.plus(other: Queue<S>) =
        QueueCompose(this, other)