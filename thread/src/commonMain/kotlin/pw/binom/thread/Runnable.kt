package pw.binom.thread

interface Runnable {
    fun run()
}

fun Runnable(runnable: () -> Unit) = object : Runnable {
    override fun run() {
        runnable()
    }

}