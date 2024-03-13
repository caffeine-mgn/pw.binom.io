package pw.binom.memory

expect fun ByteArray.fill(value: Byte)
fun ByteArray.zero() = fill(0)
