@file:JvmName("ThrowableCommon")

package pw.binom

import kotlin.jvm.JvmName

expect val Throwable.stackTrace: List<String>

fun Throwable.printStacktrace(output: Appendable = Console.err) {
    output.append("Exception $this\n")
    stackTrace.forEach {
        output.append("    at $it\n")
    }
    val cause = this.cause
    if (cause != null && cause !== this) {
        output.append("Caused by: ")
        cause.printStacktrace(output)
    }
}