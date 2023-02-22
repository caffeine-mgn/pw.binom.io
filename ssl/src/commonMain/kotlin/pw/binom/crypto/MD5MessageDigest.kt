package pw.binom.crypto

import pw.binom.security.MessageDigest

expect class MD5MessageDigest : MessageDigest {
    constructor()

    companion object
}

private fun bytes2u(inp: Byte): Int {
    return inp.toInt() and 0xff
}

private fun getMD5() = MD5MessageDigest()

private fun clearbits(bits: ByteArray) {
    for (i in bits.indices) {
        bits[i] = 0
    }
}

private const val itoa64 = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
private fun to64(v: Long, size: Int): String {
    var v = v
    var size = size
    val result = StringBuilder()
    while (--size >= 0) {
        result.append(itoa64[(v and 0x3f).toInt()])
        v = v ushr 6
    }
    return result.toString()
}

fun MD5MessageDigest.Companion.crypt(password: String, salt: String, magic: String = "$1$"): String {
    /* This string is magic for this algorithm.  Having it this way,
     * we can get get better later on */

    /* This string is magic for this algorithm.  Having it this way,
     * we can get get better later on */
    var finalState: ByteArray
    val ctx: MessageDigest
    val ctx1: MessageDigest
    var l: Long

    /* -- */

    /* Refine the Salt first */

    /* If it starts with the magic string, then skip that */

    /* -- */

    /* Refine the Salt first */
    var salt = salt
    /* If it starts with the magic string, then skip that */
    if (salt.startsWith(magic)) {
        salt = salt.substring(magic.length)
    }

    /* It stops at the first '$', max 8 chars */

    /* It stops at the first '$', max 8 chars */
    if (salt.indexOf('$') !== -1) {
        salt = salt.substring(0, salt.indexOf('$'))
    }

    if (salt.length > 8) {
        salt = salt.substring(0, 8)
    }

    ctx = getMD5()

    ctx.update(password.encodeToByteArray()) // The password first, since that is what is most unknown

    ctx.update(magic.encodeToByteArray()) // Then our magic string

    ctx.update(salt.encodeToByteArray()) // Then the raw salt

    /* Then just as many characters of the MD5(pw,salt,pw) */

    /* Then just as many characters of the MD5(pw,salt,pw) */ctx1 = getMD5()
    ctx1.update(password.encodeToByteArray())
    ctx1.update(salt.encodeToByteArray())
    ctx1.update(password.encodeToByteArray())
    finalState = ctx1.finish()
    var pl: Int = password.length
    while (pl > 0) {
        ctx.update(finalState, 0, if (pl > 16) 16 else pl)
        pl -= 16
    }

    /* the original code claimed that finalState was being cleared
       to keep dangerous bits out of memory, but doing this is also
       required in order to get the right output. */

    /* the original code claimed that finalState was being cleared
       to keep dangerous bits out of memory, but doing this is also
       required in order to get the right output. */
    clearbits(finalState)

    /* Then something really weird... */

    /* Then something really weird... */
    var i: Int = password.length
    while (i != 0) {
        if (i and 1 != 0) {
            ctx.update(finalState, 0, 1)
        } else {
            ctx.update(password.encodeToByteArray(), 0, 1)
        }
        i = i ushr 1
    }

    finalState = ctx.finish()

    /*
     * and now, just to make sure things don't run too fast
     * On a 60 Mhz Pentium this takes 34 msec, so you would
     * need 30 seconds to build a 1000 entry dictionary...
     *
     * (The above timings from the C version)
     */

    /*
     * and now, just to make sure things don't run too fast
     * On a 60 Mhz Pentium this takes 34 msec, so you would
     * need 30 seconds to build a 1000 entry dictionary...
     *
     * (The above timings from the C version)
     */for (i in 0..999) {
        ctx1.init()
        if (i and 1 != 0) {
            ctx1.update(password.encodeToByteArray())
        } else {
            ctx1.update(finalState, 0, 16)
        }
        if (i % 3 != 0) {
            ctx1.update(salt.encodeToByteArray())
        }
        if (i % 7 != 0) {
            ctx1.update(password.encodeToByteArray())
        }
        if (i and 1 != 0) {
            ctx1.update(finalState, 0, 16)
        } else {
            ctx1.update(password.encodeToByteArray())
        }
        finalState = ctx1.finish()
    }

    /* Now make the output string */

    /* Now make the output string */
    val result = StringBuilder()

    result.append(magic)
    result.append(salt)
    result.append("$")

    l = (bytes2u(finalState[0]) shl 16 or (bytes2u(finalState[6]) shl 8) or bytes2u(finalState[12])).toLong()
    result.append(to64(l, 4))

    l = (bytes2u(finalState[1]) shl 16 or (bytes2u(finalState[7]) shl 8) or bytes2u(finalState[13])).toLong()
    result.append(to64(l, 4))

    l = (bytes2u(finalState[2]) shl 16 or (bytes2u(finalState[8]) shl 8) or bytes2u(finalState[14])).toLong()
    result.append(to64(l, 4))

    l = (bytes2u(finalState[3]) shl 16 or (bytes2u(finalState[9]) shl 8) or bytes2u(finalState[15])).toLong()
    result.append(to64(l, 4))

    l = (bytes2u(finalState[4]) shl 16 or (bytes2u(finalState[10]) shl 8) or bytes2u(finalState[5])).toLong()
    result.append(to64(l, 4))

    l = bytes2u(finalState[11]).toLong()
    result.append(to64(l, 2))

    /* Don't leave anything around in vm they could use. */

    /* Don't leave anything around in vm they could use. */clearbits(finalState)

    return result.toString()
}
