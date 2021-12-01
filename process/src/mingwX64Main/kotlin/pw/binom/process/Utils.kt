package pw.binom.process

import platform.posix.exit

actual fun exitProcess(status: Int){
    kotlin.system.exitProcess()
    exit(status)
}