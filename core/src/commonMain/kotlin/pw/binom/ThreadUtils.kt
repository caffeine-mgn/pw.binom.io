package pw.binom

/**
 * Block current thread on [time] milliseconds
 *
 * @param time time to block current thread. In milliseconds
 */
expect fun sleep(time: Long)

/**
 * Returns current time in milliseconds
 *
 * @return current time in milliseconds
 */
expect fun currentTimeMillis(): Long