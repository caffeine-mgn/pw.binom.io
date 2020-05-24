package pw.binom.db.sqlite

import pw.binom.io.file.File
import pw.binom.io.use
import kotlin.test.Test

class SQLiteConnectorTest {

    val sql = "CREATE TABLE IF NOT EXISTS COMPANY(ID INT PRIMARY KEY NOT NULL, NAME TEXT NOT NULL)"

    @Test
    fun test() {
        SQLiteConnector.openFile(File("file.db")).use {
            println("createStatement")
            it.createStatement().use {
                it.executeUpdate(sql)

                it.executeQuery("select * from company").use {
                    while (it.next()) {
                        println("->>${it.getInt(0)}  \"${it.getString(1)}\"")
                    }
                }
            }

            println("prepareStatement")
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