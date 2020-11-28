package pw.binom.io

import kotlin.experimental.and
import kotlin.jvm.Synchronized

class Sha1 : MessageDigest {
    private var state = IntArray(5)
    private var count: Long
    var digestBits: ByteArray
    var digestValid: Boolean

    @Synchronized
    override fun update(input: ByteArray, offset: Int, len: Int) {
        super.update(input, offset, len)
    }

    fun updateASCII(input: String) {
        var i: Int
        val len: Int
        var x: Byte
        len = input.length
        i = 0
        while (i < len) {
            x = (input[i].toInt() and 0xff).toByte()
            update(x)
            i++
        }
    }

    /*
      * The following array forms the basis for the transform
      * buffer. Update puts bytes into this buffer and then
      * transform adds it into the state of the digest.
      */
    private var block: IntArray? = IntArray(16)
    private var blockIndex = 0

    /*
      * These functions are taken out of #defines in Steve's
      * code. Java doesn't have a preprocessor so the first
      * step is to just promote them to real methods.
      * Later we can optimize them out into inline code,
      * note that by making them final some compilers will
      * inline them when given the -O flag.
      */
    fun rol(value: Int, bits: Int): Int {
        return value shl bits or (value ushr 32 - bits)
    }

    fun blk0(i: Int): Int {
        block!![i] = rol(block!![i], 24) and -0xff0100 or (rol(block!![i], 8) and 0x00FF00FF)
        return block!![i]
    }

    fun blk(i: Int): Int {
        block!![i and 15] = rol(block!![i + 13 and 15] xor block!![i + 8 and 15] xor
                block!![i + 2 and 15] xor block!![i and 15], 1)
        return block!![i and 15]
    }

    fun R0(data: IntArray, v: Int, w: Int, x: Int, y: Int, z: Int, i: Int) {
        data[z] += (data[w] and (data[x] xor data[y]) xor data[y]) +
                blk0(i) + 0x5A827999 + rol(data[v], 5)
        data[w] = rol(data[w], 30)
    }

    fun R1(data: IntArray, v: Int, w: Int, x: Int, y: Int, z: Int, i: Int) {
        data[z] += (data[w] and (data[x] xor data[y]) xor data[y]) +
                blk(i) + 0x5A827999 + rol(data[v], 5)
        data[w] = rol(data[w], 30)
    }

    fun R2(data: IntArray, v: Int, w: Int, x: Int, y: Int, z: Int, i: Int) {
        data[z] += (data[w] xor data[x] xor data[y]) +
                blk(i) + 0x6ED9EBA1 + rol(data[v], 5)
        data[w] = rol(data[w], 30)
    }

    fun R3(data: IntArray, v: Int, w: Int, x: Int, y: Int, z: Int, i: Int) {
        data[z] += (data[w] or data[x] and data[y] or (data[w] and data[x])) +
                blk(i) + -0x70e44324 + rol(data[v], 5)
        data[w] = rol(data[w], 30)
    }

    fun R4(data: IntArray, v: Int, w: Int, x: Int, y: Int, z: Int, i: Int) {
        data[z] += (data[w] xor data[x] xor data[y]) +
                blk(i) + -0x359d3e2a + rol(data[v], 5)
        data[w] = rol(data[w], 30)
    }

    /*
      * Steve's original code and comments :
      *
      * blk0() and blk() perform the initial expand.
      * I got the idea of expanding during the round function from SSLeay
      *
      * #define blk0(i) block->l[i]
      * #define blk(i) (block->l[i&15] = rol(block->l[(i+13)&15]^block->l[(i+8)&15] \
      *   ^block->l[(i+2)&15]^block->l[i&15],1))
      *
      * (R0+R1), R2, R3, R4 are the different operations used in SHA1
      * #define R0(v,w,x,y,z,i) z+=((w&(x^y))^y)+blk0(i)+0x5A827999+rol(v,5);w=rol(w,30);
      * #define R1(v,w,x,y,z,i) z+=((w&(x^y))^y)+blk(i)+0x5A827999+rol(v,5);w=rol(w,30);
      * #define R2(v,w,x,y,z,i) z+=(w^x^y)+blk(i)+0x6ED9EBA1+rol(v,5);w=rol(w,30);
      * #define R3(v,w,x,y,z,i) z+=(((w|x)&y)|(w&x))+blk(i)+0x8F1BBCDC+rol(v,5);w=rol(w,30);
      * #define R4(v,w,x,y,z,i) z+=(w^x^y)+blk(i)+0xCA62C1D6+rol(v,5);w=rol(w,30);
      */
    var dd = IntArray(5)
    fun transform() {
        /* Copy context->state[] to working vars */
        dd[0] = state[0]
        dd[1] = state[1]
        dd[2] = state[2]
        dd[3] = state[3]
        dd[4] = state[4]
        /* 4 rounds of 20 operations each. Loop unrolled. */R0(dd, 0, 1, 2, 3, 4, 0)
        R0(dd, 4, 0, 1, 2, 3, 1)
        R0(dd, 3, 4, 0, 1, 2, 2)
        R0(dd, 2, 3, 4, 0, 1, 3)
        R0(dd, 1, 2, 3, 4, 0, 4)
        R0(dd, 0, 1, 2, 3, 4, 5)
        R0(dd, 4, 0, 1, 2, 3, 6)
        R0(dd, 3, 4, 0, 1, 2, 7)
        R0(dd, 2, 3, 4, 0, 1, 8)
        R0(dd, 1, 2, 3, 4, 0, 9)
        R0(dd, 0, 1, 2, 3, 4, 10)
        R0(dd, 4, 0, 1, 2, 3, 11)
        R0(dd, 3, 4, 0, 1, 2, 12)
        R0(dd, 2, 3, 4, 0, 1, 13)
        R0(dd, 1, 2, 3, 4, 0, 14)
        R0(dd, 0, 1, 2, 3, 4, 15)
        R1(dd, 4, 0, 1, 2, 3, 16)
        R1(dd, 3, 4, 0, 1, 2, 17)
        R1(dd, 2, 3, 4, 0, 1, 18)
        R1(dd, 1, 2, 3, 4, 0, 19)
        R2(dd, 0, 1, 2, 3, 4, 20)
        R2(dd, 4, 0, 1, 2, 3, 21)
        R2(dd, 3, 4, 0, 1, 2, 22)
        R2(dd, 2, 3, 4, 0, 1, 23)
        R2(dd, 1, 2, 3, 4, 0, 24)
        R2(dd, 0, 1, 2, 3, 4, 25)
        R2(dd, 4, 0, 1, 2, 3, 26)
        R2(dd, 3, 4, 0, 1, 2, 27)
        R2(dd, 2, 3, 4, 0, 1, 28)
        R2(dd, 1, 2, 3, 4, 0, 29)
        R2(dd, 0, 1, 2, 3, 4, 30)
        R2(dd, 4, 0, 1, 2, 3, 31)
        R2(dd, 3, 4, 0, 1, 2, 32)
        R2(dd, 2, 3, 4, 0, 1, 33)
        R2(dd, 1, 2, 3, 4, 0, 34)
        R2(dd, 0, 1, 2, 3, 4, 35)
        R2(dd, 4, 0, 1, 2, 3, 36)
        R2(dd, 3, 4, 0, 1, 2, 37)
        R2(dd, 2, 3, 4, 0, 1, 38)
        R2(dd, 1, 2, 3, 4, 0, 39)
        R3(dd, 0, 1, 2, 3, 4, 40)
        R3(dd, 4, 0, 1, 2, 3, 41)
        R3(dd, 3, 4, 0, 1, 2, 42)
        R3(dd, 2, 3, 4, 0, 1, 43)
        R3(dd, 1, 2, 3, 4, 0, 44)
        R3(dd, 0, 1, 2, 3, 4, 45)
        R3(dd, 4, 0, 1, 2, 3, 46)
        R3(dd, 3, 4, 0, 1, 2, 47)
        R3(dd, 2, 3, 4, 0, 1, 48)
        R3(dd, 1, 2, 3, 4, 0, 49)
        R3(dd, 0, 1, 2, 3, 4, 50)
        R3(dd, 4, 0, 1, 2, 3, 51)
        R3(dd, 3, 4, 0, 1, 2, 52)
        R3(dd, 2, 3, 4, 0, 1, 53)
        R3(dd, 1, 2, 3, 4, 0, 54)
        R3(dd, 0, 1, 2, 3, 4, 55)
        R3(dd, 4, 0, 1, 2, 3, 56)
        R3(dd, 3, 4, 0, 1, 2, 57)
        R3(dd, 2, 3, 4, 0, 1, 58)
        R3(dd, 1, 2, 3, 4, 0, 59)
        R4(dd, 0, 1, 2, 3, 4, 60)
        R4(dd, 4, 0, 1, 2, 3, 61)
        R4(dd, 3, 4, 0, 1, 2, 62)
        R4(dd, 2, 3, 4, 0, 1, 63)
        R4(dd, 1, 2, 3, 4, 0, 64)
        R4(dd, 0, 1, 2, 3, 4, 65)
        R4(dd, 4, 0, 1, 2, 3, 66)
        R4(dd, 3, 4, 0, 1, 2, 67)
        R4(dd, 2, 3, 4, 0, 1, 68)
        R4(dd, 1, 2, 3, 4, 0, 69)
        R4(dd, 0, 1, 2, 3, 4, 70)
        R4(dd, 4, 0, 1, 2, 3, 71)
        R4(dd, 3, 4, 0, 1, 2, 72)
        R4(dd, 2, 3, 4, 0, 1, 73)
        R4(dd, 1, 2, 3, 4, 0, 74)
        R4(dd, 0, 1, 2, 3, 4, 75)
        R4(dd, 4, 0, 1, 2, 3, 76)
        R4(dd, 3, 4, 0, 1, 2, 77)
        R4(dd, 2, 3, 4, 0, 1, 78)
        R4(dd, 1, 2, 3, 4, 0, 79)
        /* Add the working vars back into context.state[] */state[0] += dd[0]
        state[1] += dd[1]
        state[2] += dd[2]
        state[3] += dd[3]
        state[4] += dd[4]
    }

    override fun init() {
        /* SHA1 initialization constants */
        state[0] = 0x67452301
        state[1] = -0x10325477
        state[2] = -0x67452302
        state[3] = 0x10325476
        state[4] = -0x3c2d1e10
        count = 0
        digestBits = ByteArray(20)
        digestValid = false
        blockIndex = 0
    }

    override fun update(b: Byte) {
        val mask = 8 * (blockIndex and 3)
        count += 8
        block!![blockIndex shr 2] = block!![blockIndex shr 2] and (0xff shl mask).inv()
        block!![blockIndex shr 2] = block!![blockIndex shr 2] or (b.toInt() and 0xff shl mask)
        blockIndex++
        if (blockIndex == 64) {
            transform()
            blockIndex = 0
        }
    }

    override fun finish(): ByteArray {
        val bits = ByteArray(8)
        var i: Int
        var j: Int
        i = 0
        while (i < 8) {
            bits[i] = (count ushr (7 - i) * 8 and 0xff).toByte()
            i++
        }
        update(128.toByte())
        while (blockIndex != 56) {
            update(0.toByte())
        }
        // This should cause a transform to happen.
        update(bits)
        i = 0
        while (i < 20) {
            digestBits[i] = (state[i shr 2] shr (3 - (i and 3)) * 8 and 0xff).toByte()
            i++
        }
        digestValid = true
        return digestBits
    }

    val alg: String
        get() = "SHA1"

    fun digout(): String {
        val sb = StringBuilder()
        for (i in 0..19) {
            var c1: Char
            var c2: Char
            c1 = (digestBits[i].toInt() ushr 4 and 0xf).toChar()
            c2 = (digestBits[i] and 0xf).toChar()
            c1 = (if (c1.toInt() > 9) 'a'.toInt() + (c1.toInt() - 10) else '0' + c1.toInt()) as Char
            c2 = (if (c2.toInt() > 9) 'a'.toInt() + (c2.toInt() - 10) else '0' + c2.toInt()) as Char
            sb.append(c1)
            sb.append(c2)
            /*            if (((i+1) % 4) == 0)
                           sb.append(' '); */
        }
        return sb.toString()
    }

    init {
        state = IntArray(5)
        count = 0
        if (block == null) block = IntArray(16)
        digestBits = ByteArray(20)
        digestValid = false
        init()
    }
}