package pw.binom.db.sqlite

import pw.binom.UUID
import pw.binom.io.use
import pw.binom.nextUuid
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class SQLiteConnectorTest {
    companion object {
        const val SIMPLE_COMPANY_TABLE =
            "CREATE TABLE IF NOT EXISTS COMPANY(ID INT PRIMARY KEY NOT NULL, NAME TEXT NOT NULL, uid blob not null)"
    }

    @Test
    fun test2() {
        SQLiteConnector.memory().use {
            it.createStatement().use {
                it.executeUpdate(SIMPLE_COMPANY_TABLE)
            }

            repeat(5000) { num ->
                it.prepareStatement("insert into company (id,name,uid) values(?,?,?)").use {
                    it.set(0, num)
                    val s = UUID.random()
                    it.set(1, s.toString())
                    val buf = ByteArray(16)
                    s.toByteArray(buf)
                    it.set(2, buf)
                    it.executeUpdate()
                }
//                    it.commit()
            }
        }
    }

    @Test
    fun commitTest() {
        SQLiteConnector.memory().use { db ->
            db.createStatement().use {
                it.executeUpdate(SIMPLE_COMPANY_TABLE)
            }

            db.beginTransaction()
            db.prepareStatement("insert into company (id,name,uid) values(?,?,?)").use {
                repeat(100) { index ->
                    it.set(0, index)
                    it.set(1, Random.nextUuid().toString())
                    it.set(2, Random.nextUuid())
                    it.executeUpdate()
                }
            }
            db.commit()

            var count = 0
            db.prepareStatement("select count(*) from company").use {
                it.executeQuery().use { q ->
                    while (q.next()) {
                        count = q.getInt(0) ?: 0
                    }
                }
            }
            assertEquals(100, count)
        }
    }

    @Test
    fun test() {
        SQLiteConnector.memory().use {
            it.createStatement().use {
                it.executeUpdate(SIMPLE_COMPANY_TABLE)

                it.executeQuery("select * from company").use {
                    while (it.next()) {
                        println("->>${it.getInt(0)}  \"${it.getString(1)}\"  ${it.getUUID(2)}")
                    }
                }
            }

            it.prepareStatement("insert into company (id,name,uid) values(?,?,?)").use {
                val s =
                    it.set(0, 1)
                it.set(1, s.toString())
                it.set(2, UUID.random())
                it.executeUpdate()
            }

            it.prepareStatement("select * from company where id=?").use {
                it.set(0, 1)
                it.executeQuery().use {
                    while (it.next()) {
                        println("--->${it.getInt(0)}")
                    }
                }
            }
        }
    }
}
