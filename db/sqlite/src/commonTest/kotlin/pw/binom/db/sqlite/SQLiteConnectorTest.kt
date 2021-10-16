package pw.binom.db.sqlite

import pw.binom.UUID
import pw.binom.io.file.File
import pw.binom.io.use
import kotlin.test.Test

class SQLiteConnectorTest {
companion object {
    const val SIMPLE_COMPANY_TABLE =
        "CREATE TABLE IF NOT EXISTS COMPANY(ID INT PRIMARY KEY NOT NULL, NAME TEXT NOT NULL, uid blob not null)"
}

    @Test
    fun test2() {
        val f = File("file.db")
        try {
            SQLiteConnector.openFile(f).use {
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
        } finally {
            f.delete()
        }
    }

    @Test
    fun test() {
        val f = File("file.db")
        try {
            SQLiteConnector.openFile(f).use {
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
        } finally {
            f.delete()
        }
    }
}