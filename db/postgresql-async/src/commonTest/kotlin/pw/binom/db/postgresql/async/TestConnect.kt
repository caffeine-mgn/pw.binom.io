package pw.binom.db.postgresql.async

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import pw.binom.UUID
import pw.binom.async
import pw.binom.charset.Charsets
import pw.binom.concurrency.Worker
import pw.binom.concurrency.sleep
import pw.binom.db.ResultSet
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.io.use
import kotlin.math.absoluteValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.measureTime
import kotlin.time.seconds

class TestConnect {

    //    @Test
    fun test() {
        val manager = SocketNIOManager()
        async {
            try {
                println("Connection...")
                val con = PGConnection.connect(
                    host = "127.0.0.1",
                    port = 5432,
                    manager = manager,
                    charset = Charsets.UTF8,
                    userName = "postgres",
                    password = "postgres",
                    dataBase = "sellsystem"
                )
                println("Connected!")

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
            manager.update()
        }
    }

    @OptIn(ExperimentalTime::class)
    fun pg(func: suspend (PGConnection) -> Unit) {
        val manager = SocketNIOManager()
        var done = false
        var exception: Throwable? = null
        async {
            var con: PGConnection
            val now = TimeSource.Monotonic.markNow()
            while (true) {
                try {
                    con = PGConnection.connect(
                        host = "127.0.0.1",
                        port = 25331,
                        manager = manager,
                        charset = Charsets.UTF8,
                        userName = "postgres",
                        password = "postgres",
                        dataBase = "test"
                    )
                    break
                } catch (e: Throwable) {
                    if (now.elapsedNow() > 10.seconds) {
                        exception = RuntimeException("Connection Timeout")
                        return@async
                    }
                    Worker.sleep(100)
                }
            }
            try {
                func(con)
            } catch (e: Throwable) {
                exception = e
            } finally {
                con.close()
                done = true
            }
        }

        while (!done) {
            manager.update(500)
        }
        if (exception != null) {
            throw exception!!
        }
    }

    //    @Test
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