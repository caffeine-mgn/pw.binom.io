package pw.binom.db.sqlite

import pw.binom.UUID
import pw.binom.io.file.File
import pw.binom.io.use
import kotlin.test.Test

class SQLiteConnectorTest {

    val sql = "CREATE TABLE IF NOT EXISTS COMPANY(ID INT PRIMARY KEY NOT NULL, NAME TEXT NOT NULL, uid blob not null)"

    @Test
    fun test() {
        SQLiteConnector.openFile(File("file.db")).use {
            it.createStatement().use {
                it.executeUpdate(sql)

                it.executeQuery("select * from company").use {
                    while (it.next()) {
                        println("->>${it.getInt(0)}  \"${it.getString(1)}\"  ${UUID.create(it.getBlob(2))}")
                    }
                }
            }

            it.prepareStatement("insert into company (name,uid) values(?,?)").use {
                val s = UUID.random()
                it.set(0, s.toString())
                val buf = ByteArray(16)
                s.toByteArray(buf)
                it.set(1, buf)
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