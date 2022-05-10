package pw.binom

expect abstract class Thread {
    val id: Long
    abstract fun execute()
    fun start()
    fun join()
}
