package pw.binom.db.sqlite

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import pw.binom.io.useAsync
import kotlin.test.Test
import kotlin.test.assertFalse

class AsyncSQLiteConnectorTest {
  @Test
  fun test() =
    runTest {
      withContext(Dispatchers.Default) {
        val mem = AsyncSQLiteConnector.memory()
        mem.createStatement().useAsync {
          it.executeUpdate(SQLiteConnectorTest.SIMPLE_COMPANY_TABLE)
        }
        mem.prepareStatement("insert into COMPANY")
        mem.prepareStatement("select * from company").useAsync {
          it.executeQuery().useAsync {
            assertFalse(it.next())
            println("OK")
          }
        }
      }
    }
}
