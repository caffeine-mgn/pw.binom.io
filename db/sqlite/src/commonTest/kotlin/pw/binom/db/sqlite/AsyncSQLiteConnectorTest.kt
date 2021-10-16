package pw.binom.db.sqlite

import pw.binom.async2
import pw.binom.concurrency.joinAndGetOrThrow
import pw.binom.io.use
import pw.binom.network.NetworkDispatcher
import kotlin.test.Test
import kotlin.test.assertFalse

class AsyncSQLiteConnectorTest {

    @Test
    fun test() {
        val nd = NetworkDispatcher()
        nd.runSingle {
            val mem = AsyncSQLiteConnector.memory("123")
            mem.createStatement().use {
                it.executeUpdate(SQLiteConnectorTest.SIMPLE_COMPANY_TABLE)
            }
            mem.prepareStatement("select * from company").use {
                it.executeQuery().use {
                    assertFalse(it.next())
                    println("OK")
                }
            }
            Unit
        }
    }
}