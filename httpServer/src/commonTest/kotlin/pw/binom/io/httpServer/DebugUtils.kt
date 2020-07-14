package pw.binom.io.httpServer

import pw.binom.ByteBuffer
import pw.binom.forEachIndexed

fun Byte.toChar2() = when (this) {
    '\r'.toByte() -> "\\r"
    '\n'.toByte() -> "\\n"
    '\t'.toByte() -> "\\t"
    0.toByte() -> "--"
    else -> " " + this.toChar()
}

fun ByteBuffer.print() {
    val p = position
    val l = limit

    clear()
    forEachIndexed { i, byte ->
        if (i > 0)
            print(", ")
        when {
            i == p && i == l -> print("[] ${byte.toChar2()}")
            i == p -> print("[${byte.toChar2()}")
            i == l -> print("]${byte.toChar2()}")
            else -> print("${byte.toChar2()}")
        }
    }
    if (l == capacity)
        print("]")
    println()
    limit = l
    position = p
}