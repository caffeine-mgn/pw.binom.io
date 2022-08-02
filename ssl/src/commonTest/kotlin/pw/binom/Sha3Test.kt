package pw.binom

import pw.binom.crypto.Keccak256MessageDigest
import pw.binom.io.socket.ssl.toHex
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

@Ignore
class Sha3Test {

    fun asByte(m: Int, n: Int): Byte = (m shl 4 or n).toByte()

    @Test
    fun tt() {
        val inputTxt = "My message!"
//        val input = inputTxt.encodeToByteArray()
        val input = byteArrayOf(
            asByte(0x6, 0x8),
            asByte(0x6, 0x5),
            asByte(0x6, 0xc),
            asByte(0x6, 0xc),
            asByte(0x6, 0xf),
            asByte(0x2, 0x0),
            asByte(0x7, 0x7),
            asByte(0x6, 0xf),
            asByte(0x7, 0x2),
            asByte(0x6, 0xc),
            asByte(0x6, 0x4),
        )
//        val expected = "3f89843acdac294c99297ae2f9c8c43887729c121bb854a7a85eb67de07b5cff".hexToByteArray()
        val expected = byteArrayOf(
            asByte(0x4, 0x7),
            asByte(0x1, 0x7),
            asByte(0x3, 0x2),
            asByte(0x8, 0x5),
            asByte(0xa, 0x8),
            asByte(0xd, 0x7),
            asByte(0x3, 0x4),
            asByte(0x1, 0xe),
            asByte(0x5, 0xe),
            asByte(0x9, 0x7),
            asByte(0x2, 0xf),
            asByte(0xc, 0x6),
            asByte(0x7, 0x7),
            asByte(0x2, 0x8),
            asByte(0x6, 0x3),
            asByte(0x8, 0x4),
            asByte(0xf, 0x8),
            asByte(0x0, 0x2),
            asByte(0xf, 0x8),
            asByte(0xe, 0xf),
            asByte(0x4, 0x2),
            asByte(0xa, 0x5),
            asByte(0xe, 0xc),
            asByte(0x5, 0xf),
            asByte(0x0, 0x3),
            asByte(0xb, 0xb),
            asByte(0xf, 0xa),
            asByte(0x2, 0x5),
            asByte(0x4, 0xc),
            asByte(0xb, 0x0),
            asByte(0x1, 0xf),
            asByte(0xa, 0xd)
        )
        val d = Keccak256MessageDigest()
        d.update(input)
        val result = d.finish()
        println("$inputTxt->${result.toHex()}")
//        val result: ByteArray = Hash.sha3(input)
        assertEquals(expected.size, result.size)
        result.forEachIndexed { index, byte ->
            assertEquals(expected[index], byte, "Fail in $index")
        }
    }
}
