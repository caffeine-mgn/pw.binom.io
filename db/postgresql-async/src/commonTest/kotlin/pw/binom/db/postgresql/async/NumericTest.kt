package pw.binom.db.postgresql.async

import kotlinx.coroutines.test.runTest
import pw.binom.charset.Charsets
import pw.binom.date.iso8601
import pw.binom.io.socket.InetNetworkAddress
import pw.binom.io.use
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class NumericTest {

    @Ignore
    @Test
    fun test3() = runTest {
        val connection = PGConnection.connect(
            address = InetNetworkAddress.create(
                host = "localhost",
                port = 5432,
            ),
            charset = Charsets.UTF8,
            userName = "postgres",
            password = "postgres",
            dataBase = "sellsystem",
        )
        connection.prepareStatement("select birthday from buyers where id=279734").use { r ->
            r.executeQuery().use { b ->
                assertTrue(b.next())
                println("->${b.getDate("birthday")!!.iso8601()}")
            }
        }
        println("Connected!")
        connection.asyncClose()
    }
//
//    @Test
//    fun test() {
//        val data1 =
//            "00 02 00 00 00 00 00 02 00 7b 11 30".split(' ').map { it.toUByte(16) }.toUByteArray()
//                .toByteArray()
//
//        assertEquals(123.44, NumericUtils.decode(data1).toStringExpanded().toDouble())
//
//        val data2 =
//            "00 02 00 00 00 00 00 01 00 01 13 88".split(' ').map { it.toUByte(16) }.toUByteArray()
//                .toByteArray()
//
//        assertEquals(1.5, NumericUtils.decode(data2).toString().toDouble())
//    }
}
