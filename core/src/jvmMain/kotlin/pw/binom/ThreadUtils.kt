package pw.binom

actual fun sleep(time: Long) {
    Thread.sleep(time)
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()