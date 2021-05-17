package pw.binom.concurrency

import pw.binom.System
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@ExperimentalTime
object DelayWorker {
    private val lock = Lock()
    private val condition = lock.newCondition()
    private val w = Worker()
    private val tasks = ArrayList<Delay>()

    init {
        w.execute(Unit) {
            while (true) {
                while (tasks.isEmpty()) {//ждем пока появятся задачи
                    condition.await()
                }
                val first = tasks.sortedBy { it.time }[0]
                condition.await(Duration.seconds(0.0))
            }
        }
    }

    class Delay(val time: Long) {
        fun exe() {

        }
    }
}