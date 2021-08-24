package pw.binom.concurrency

import platform.posix.usleep

actual fun sleep(millis: Long){
    usleep((millis * 1000).toUInt())
}