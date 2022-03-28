package pw.binom.db.sqlite

import kotlinx.coroutines.test.runTest
import pw.binom.io.use
import kotlin.test.Test
import kotlin.test.assertFalse

class AsyncSQLiteConnectorTest {

    @Test
    fun test() = runTest {
        val mem = AsyncSQLiteConnector.memory()
        mem.createStatement().use {
            it.executeUpdate(SQLiteConnectorTest.SIMPLE_COMPANY_TABLE)
        }
        mem.prepareStatement("select * from company").use {
            it.executeQuery().use {
                assertFalse(it.next())
                println("OK")
            }
        }
    }
}
