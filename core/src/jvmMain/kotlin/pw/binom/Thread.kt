package pw.binom

actual object Thread {
    /**
     * Block current thread on [time] milliseconds
     *
     * @param time time to block current thread. In milliseconds
     */
    actual fun sleep(time: Long) {
        java.lang.Thread.sleep(time)
    }

    /**
     * Returns current time in milliseconds
     *
     * @return current time in milliseconds
     */
    actual fun currentTimeMillis(): Long = System.currentTimeMillis()

    /**
     * Returns current thread id
     */
    actual val id: Long
        get() = java.lang.Thread.currentThread().id

}