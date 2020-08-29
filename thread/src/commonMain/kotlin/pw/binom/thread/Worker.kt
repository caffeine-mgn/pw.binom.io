package pw.binom.thread

expect class Worker {
    constructor(name: String? = null)

    fun <DATA, RESULT> execute(input: DATA, func: (DATA) -> RESULT): Future<RESULT>
    fun requestTermination(): Future<Unit>
    val isInterrupted: Boolean
    val id: Long

    companion object {
        val current: Worker?
    }
}

expect fun Worker.Companion.sleep(deley: Long)