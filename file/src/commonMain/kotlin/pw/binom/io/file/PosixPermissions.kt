package pw.binom.io.file

import kotlin.jvm.JvmInline

@JvmInline
value class PosixPermissions(val mode: Int) {
    companion object {
        fun parse(permissions: String) {
        }
    }
}
