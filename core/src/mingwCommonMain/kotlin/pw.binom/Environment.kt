package pw.binom

import kotlinx.cinterop.*
import platform.posix.*

actual val Environment.workDirectory: String
    get() {
        val data = _wgetcwd(null, 0.convert()) ?: TODO()
        try {
            return data.toKString()
        } finally {
            free(data)
        }
    }