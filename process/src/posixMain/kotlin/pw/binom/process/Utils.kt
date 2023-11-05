package pw.binom.process

import platform.posix.exit

actual fun exitProcess(status: Int) {
  exit(status)
}
