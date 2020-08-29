package pw.binom.thread

import platform.posix.usleep

actual fun Worker.Companion.sleep(deley: Long){
    usleep((deley * 1000).toUInt())
}