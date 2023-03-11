package pw.binom.network.lite

import pw.binom.io.Buffer
import pw.binom.io.socket.NetworkAddress

fun Buffer.Companion.BlockCopy(src: ByteArray, srcOffset: Int, dst: ByteArray, dstOffset: Int, count: Int) {
    src.copyInto(dst, dstOffset, srcOffset + count)
}
typealias IPEndPoint = NetworkAddress

inline val Short.asChar
    get() = toInt().toChar()

inline val Char.asShort
    get() = code.toShort()

inline val Char.asUShort
    get() = asShort.toUShort()

inline val UShort.asChar
    get() = toShort().asChar
