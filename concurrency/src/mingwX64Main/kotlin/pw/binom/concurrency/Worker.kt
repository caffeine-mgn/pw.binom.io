package pw.binom.concurrency

import platform.windows.Sleep

actual fun Worker.Companion.sleep(deley: Long){
    Sleep(deley.toUInt())
}