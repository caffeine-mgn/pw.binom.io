package pw.binom.strong

import kotlinx.coroutines.Job
import pw.binom.async2
import pw.binom.concurrency.sleep

fun asyncTest(func: suspend () -> Unit) {
    val r = async2(func)

    if (!r.isDone) {
        throw IllegalStateException("asyncTest must be stoped")
    }
    if (!r.isSuccess) {
        throw r.exceptionOrNull!!
    }
}

fun Job.joinForce(){
    while (isActive){
        sleep(10)
    }
}