package pw.binom

val isBigEndianPrivate = htonl(47u) == 47u

fun htonl(n: UInt): UInt {
    val x = (n and 0xff000000u) shr 24
    val result = n shl 8
    return result or x
}
