package pw.binom.concurrency

import platform.windows.Sleep

actual fun Worker.Companion.sleep(deley: Long){
    platform.windows.Sleep(deley.toUInt())
}