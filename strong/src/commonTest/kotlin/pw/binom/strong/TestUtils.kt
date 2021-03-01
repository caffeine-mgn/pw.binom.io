package pw.binom.strong

import pw.binom.async2

fun asyncTest(func: suspend () -> Unit) {
    val r = async2(func)

    if (!r.isDone) {
        throw IllegalStateException("asyncTest must be stoped")
    }
    if (!r.isSuccess) {
        throw r.exceptionOrNull!!
    }
}