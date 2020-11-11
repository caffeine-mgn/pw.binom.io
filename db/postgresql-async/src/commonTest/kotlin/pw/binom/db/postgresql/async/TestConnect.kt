package pw.binom.db.postgresql.async

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

                val ps = con.prepareStatement(
                    query = "select * from \"user\" where login=? and company_id=?",
                    paramColumnTypes = listOf(ResultSet.ColumnType.STRING, ResultSet.ColumnType.INT)
                )
                ps.set(0, "demo")
                ps.set(1, 4)
                ps.executeQuery().use {
                    println(it.columns.joinToString(" | "))
                    while (it.next()) {
                        val line = it.columns.map { name ->
                            it.getString(name)
                        }
                            .joinToString(" | ")
                        println(line)
                    }
                }

                ps.set(0, "reklama@mirteck.ru")
                ps.set(1, 147)
                ps.executeQuery().use {
                    println(it.columns.joinToString(" | "))
                    while (it.next()) {
                        val line = it.columns.map { name ->
                            it.getString(name)
                        }
                            .joinToString(" | ")
                        println(line)
                    }
                }

                ps.close()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        while (true) {
            manager.update()
        }
    }
}