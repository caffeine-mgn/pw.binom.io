package pw

import pw.binom.UUID
import pw.binom.async
import pw.binom.db.tarantool.*
import pw.binom.db.tarantool.protocol.Code
import pw.binom.db.tarantool.protocol.Key
import pw.binom.db.tarantool.protocol.QueryIterator
import pw.binom.io.socket.nio.SocketNIOManager
import kotlin.test.Test

val String.ff: String
    get() = replace("'", "\\'")

class FF {

    @Test
    fun test() {
        val manager = SocketNIOManager()

        async {
            try {
                val client = TarantoolConnection.connect(manager, "127.0.0.1", 3301, "server", "server")
                async {
                    try {
                        client.ping()

                        val meta = client.getMeta()
                        val table = meta.find { it.name == "tester" }!!
                        val id = table.format.indexOfFirst { it.name == "id" }.takeIf { it != -1 }!! + 1
                        val bandName = table.format.indexOfFirst { it.name == "band_name" }.takeIf { it != -1 }!! + 1
                        val year = table.format.indexOfFirst { it.name == "year" }.takeIf { it != -1 }!! + 1

                        println("Eval result: ${client.eval("return ...", listOf("123"))}")

                        val r = client.sql("select * from \"tester\" INDEXED BY \"primary\"", emptyList())
                        println("Sql Result: $r")
                        for (i in 0 until r.columnSize) {
                            println("->${r.getColumn(i)}")
                        }
                        println("Rows:")
                        r.forEach {
                            println("->$it")
                        }

                        val stm = client.prepare("""insert into "tester" ("id", "band_name", "year") values (?,?,?)""")
                        client.sql(stm, listOf(10, "Test Prepare", 1191))

                        val sql = """insert into "tester" ("id", "band_name", "year") values (6,'123',1888)"""
                        val sql2 = """insert into "tester" ("id", "band_name", "year") values (7,'123',1888)"""

                        val lua = """
                            box.begin()
                            x=box.execute([[${sql};]])
                            y=box.execute([[${sql2};]])
                            box.commit()
                            return x,y
                        """
                        println(lua)
                        client.eval(lua)
//                        client.sql(sql)

//                        client.insert(
//                                space = table.id,
//                                listOf(
//                                        4,
//                                        "Value From Kotlin",
//                                        1984
//                                )
//                        )
//                        client.insert(
//                                space = table.id,
//                                listOf(
//                                        5,
//                                        "Value From Kotlin",
//                                        1984
//                                )
//                        )
//                        println("--rollback")

                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }


            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        while (true) {
            manager.update(1000)
        }
    }
}