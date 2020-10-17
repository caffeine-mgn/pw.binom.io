package pw.binom.db.sqlite

import pw.binom.UUID
import pw.binom.io.file.File
import pw.binom.io.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SQLiteConnectorTest {

    val sql = "CREATE TABLE IF NOT EXISTS COMPANY(ID INT PRIMARY KEY NOT NULL, NAME TEXT, uid blob not null)"

    @Test
    fun test() {
        val file =File("file.db")
        val uuid = UUID.random()
        try {
            SQLiteConnector.openFile(file).use {
                it.createStatement().use {
                    it.executeUpdate(sql)

                    it.executeQuery("select * from company").use {
                        while (it.next()) {
                            println("->>${it.getInt(0)}  \"${it.getString(1)}\"  ${UUID.create(it.getBlob(2))}")
                        }
                    }
                }

                it.prepareStatement("insert into company (id, name,uid) values(?,?,?)").use {
                    it.set(0, 1)
                    it.set(1, uuid.toString())
                    it.set(2, uuid)
                    it.executeUpdate()

                    it.set(0, 2)
                    it.setNull(1)
                    it.set(2, uuid)
                    it.executeUpdate()
                }

                it.prepareStatement("select * from company order by id").use {
                    it.executeQuery().use {
                        assertTrue(it.next())
                        assertEquals(1, it.getInt(0))
                        assertFalse(it.isNull(1))
                        assertEquals(uuid.toString(), it.getString(1))
                        assertEquals(uuid, it.getUUID(2))


                        assertTrue(it.next())
                        assertEquals(2, it.getInt(0))
                        assertTrue(it.isNull(1))
                        assertEquals(uuid, it.getUUID(2))
                    }
                }
            }
        } finally {
            file.delete()
        }

    }
}