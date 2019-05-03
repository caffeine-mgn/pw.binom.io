package pw.binom

expect object Thread {
    /**
     * Block current thread on [time] milliseconds
     *
     * @param time time to block current thread. In milliseconds
     */
    fun sleep(time: Long)

    /**
     * Returns current time in milliseconds
     *
     * @return current time in milliseconds
     */
    fun currentTimeMillis(): Long

    /**
     * Returns current thread id
     */
    val id:Long
}