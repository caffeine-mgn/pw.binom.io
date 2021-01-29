package pw.binom.db.postgresql.async

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import pw.binom.UUID
import pw.binom.async
import pw.binom.charset.Charsets
import pw.binom.concurrency.Worker
import pw.binom.concurrency.sleep
import pw.binom.db.ResultSet
import pw.binom.io.use
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import kotlin.math.absoluteValue
import kotlin.test.*
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.measureTime
import kotlin.time.seconds

@Ignore
class TestConnect {

    //    @Test
    fun test() {
        val manager = NetworkDispatcher()
        async {
            try {
                val con = PGConnection.connect(
                    address = NetworkAddress.Immutable(
                        host = "localhost",
                        port = 5432,
                    ),
                    manager = manager,
                    charset = Charsets.UTF8,
                    userName = "postgres",
                    password = "postgres",
                    dataBase = "sellsystem"
                )

//                val msg = con.sendQuery("select now()")
                val msg = con.query("update \"user\" set login='' where login=''") as QueryResponse.Status
                println("Updated ${msg.status}")

                val msg2 = con.query("select * from \"user\" where login='demo'")
                msg2 as QueryResponse.Data
                while (msg2.next()) {
                    val row = (0 until msg2.meta.size).map {
                        msg2[it]
                    }.joinToString(" | ")
                    println("->$row")
                }
                println("DONE!")

//                con.prepareStatement(
//                    query = "select * from \"user\" where login=? and company_id=?",
//                    paramColumnTypes = listOf(ResultSet.ColumnType.STRING, ResultSet.ColumnType.INT),
//                    resultColumnTypes = listOf(
//                        ResultSet.ColumnType.LONG,
//                        ResultSet.ColumnType.STRING,
//                        ResultSet.ColumnType.LONG
//                    )
//                ).use { ps ->
//                    ps.set(0, "demo")
//                    ps.set(1, 4)
//                    ps.executeQuery().use {
//                        println(it.columns.joinToString(" | "))
//                        while (it.next()) {
//                            val line = it.columns.map { name ->
//                                it.getString(name)
//                            }
//                                .joinToString(" | ")
//                            println(line)
//                        }
//                    }
//
//                    ps.set(0, "reklama@mirteck.ru")
//                    ps.set(1, 147)
//                    ps.executeQuery().use {
//                        println(it.columns.joinToString(" | "))
//                        while (it.next()) {
//                            val line = it.columns.map { name ->
//                                it.getString(name)
//                            }
//                                .joinToString(" | ")
//                            println(line)
//                        }
//                    }
//                }


                con.prepareStatement(
                    "select * from osmi_cards_config where id=?",
                    listOf(ResultSet.ColumnType.UUID)
                ).use {
                    try {
                        it.set(0, UUID.fromString("db5d3f68-ed81-4c01-8ac8-6eb783d67eeb"))
                        it.executeQuery().use {
                            println(it.columns.joinToString(" | "))
                            while (it.next()) {
                                val line = it.columns.map { name ->
                                    it.getString(name)
                                }
                                    .joinToString(" | ")
                                println(line)
                            }
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        throw e
                    }
                }

                con.prepareStatement("SELECT TIMESTAMP '2020-01-05 15:43:36.000000'").use {
                    try {
                        it.executeQuery().use {
                            println(it.columns.joinToString(" | "))
                            while (it.next()) {
                                val line = it.columns.map { name ->
                                    it.getString(name)
                                }
                                    .joinToString(" | ")
                                println(line)
                            }
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        throw e
                    }
                }

                con.prepareStatement("select * from buyers limit 3").use {
                    try {
                        it.executeQuery().use {
                            println(it.columns.joinToString(" | "))
                            while (it.next()) {
                                val line = it.columns.map { name ->
                                    it.getString(name)
                                }
                                    .joinToString(" | ")
                                println(line)
                            }
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        throw e
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        while (true) {
            manager.select()
        }
    }

    @Test
    fun testString() {
        pg { con ->
            con.prepareStatement("""select 'Привет' """).executeQuery().also {
                it.next()
                assertEquals("Привет", it.getString(0))
            }
        }
        pg { con ->
            con.prepareStatement("""select 'ПрИвет' """).executeQuery().also {
                it.next()
                assertEquals("ПрИвет", it.getString(0))
            }
        }
    }

    @Test
    fun timestampTest() {
        pg { con ->
            con.prepareStatement("SELECT TIMESTAMP '2020-01-05 15:43:36.000000'").use {
                try {
                    it.executeQuery().use {
                        assertEquals(1, it.columns.size)
                        assertTrue(it.next())
                        assertEquals(1578239016000L, it.getDate(0)!!.time)
                        assertFalse(it.next())
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    throw e
                }
            }
        }

        pg { con ->
            con.prepareStatement("SELECT TIMESTAMPTZ '2020-01-05 15:43:36.000000'").use {
                try {
                    it.executeQuery().use {
                        assertEquals(1, it.columns.size)
                        assertTrue(it.next())
                        assertEquals(1578239016000L, it.getDate(0)!!.time)
                        assertFalse(it.next())
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    throw e
                }
            }
        }
    }

    @Test
    fun decamalTest() {
        pg { con ->
            con.prepareStatement("select 1.5").use {
                try {
                    it.executeQuery().use {
                        assertEquals(1, it.columns.size)
                        assertTrue(it.next())
                        assertEquals(1.5, it.getDouble(0))
                        assertEquals(BigDecimal.fromDouble(1.5), it.getBigDecimal(0))
                        assertFalse(it.next())

                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    throw e
                }
            }
        }
        pg { con ->
            con.prepareStatement("select 1.5f").use {
                try {
                    it.executeQuery().use {
                        assertEquals(1, it.columns.size)
                        assertTrue(it.next())
                        assertEquals(1.5, it.getDouble(0))
                        assertEquals(BigDecimal.fromDouble(1.5), it.getBigDecimal(0))
                        assertFalse(it.next())
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    throw e
                }
            }
        }
    }


}