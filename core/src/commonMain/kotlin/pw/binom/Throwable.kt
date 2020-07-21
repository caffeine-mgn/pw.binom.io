@file:JvmName("ThrowableCommon")

package pw.binom

import kotlin.jvm.JvmName

expect val Throwable.stackTrace: List<String>

fun Throwable.printStacktrace(output: Appendable = Console.err) {
    output.append("Exception $this\n")
    stackTrace.forEach {
        output.append("    at $it\n")
    }
    if (this.cause !== this) {
        this.cause!!.printStacktrace(output)
    }
}