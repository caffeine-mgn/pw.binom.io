package pw.binom.dns.protocol

import pw.binom.ByteBuffer
import pw.binom.fromBytes

internal fun String.toDnsString(): CharArray {
    var lock = 0
    val host = "$this."
    val out = CharArray(host.length)
    var pos = 0

    for (i in host.indices) {
        if (host[i] == '.') {
            out[pos++] = (i - lock).toChar()
            while (lock < i) {
                out[pos++] = host[lock]
                lock++
            }
            lock++
        }
    }
    return out
}

internal fun CharArray.fromDns(): String {
    val name = this
    var i = 0
    var p:Char

    while (i < name.size) {
        p = name[i]
        val c = p.code and 0xFF
        if (c <= size) {
            for (j in 0 until c) {
                name[i] = name[i + 1]
                i++
            }
        }
        name[i] = '.'
        i++
    }
    if (name.isEmpty()) {
        return ""
    }
    return name.concatToString(0, name.size - 1)
}

internal fun String.fromDns(): String =
    toCharArray().fromDns()

fun ByteBuffer.readDns(): CharArray {
    if (get(position).toInt() and 0xC0 != 0) {//check is reference
        val firstByte = get().toInt() and 0xF
        val second = get()


        val ptr = Short.fromBytes((firstByte.inv() or 0xC0).inv().toByte(), second)
        val pos = position
        try {
            position = ptr.toInt()
            return readDns()
        } finally {
            position = pos
        }
    }
    var endIndex = -1
    for (i in position until limit) {
        if (get(i) == 0.toByte()) {
            endIndex = i
            break
        }
    }
    if (endIndex == -1) {
        throw RuntimeException("Can't find end of dns name")
    }
    val out = CharArray(endIndex - position) {
        get().toInt().toChar()
    }
    check(get() == 0.toByte())
    return out
}

fun ByteBuffer.writeDns(text: CharArray) {
    text.forEach {
        put(it.code.toByte())
    }
    put(0.toByte())
}