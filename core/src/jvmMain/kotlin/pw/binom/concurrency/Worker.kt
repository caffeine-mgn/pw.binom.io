package pw.binom.concurrency

actual fun sleep(millis: Long) {
    Thread.sleep(millis)
}