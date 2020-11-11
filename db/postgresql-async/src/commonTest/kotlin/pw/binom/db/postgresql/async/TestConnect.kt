package pw.binom.db.postgresql.async

import pw.binom.UUID
import pw.binom.async
import pw.binom.charset.Charsets
import pw.binom.db.ResultSet
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.io.use
import kotlin.test.Test

class TestConnect {

    @Test
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


                con.prepareStatement("select * from osmi_cards_config where id=?",
                listOf(ResultSet.ColumnType.UUID)).use {
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
                    } catch (e:Throwable){
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
}