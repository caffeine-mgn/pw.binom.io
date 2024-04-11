package pw.binom.db.sqlite

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import pw.binom.date.Date
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

  @Test
  fun dateTest() =
    runTest {
      withContext(Dispatchers.Default) {
        val mem = AsyncSQLiteConnector.memory()
        val tableName = "test"
        mem.createStatement().useAsync {
          it.executeUpdate("CREATE TABLE IF NOT EXISTS $tableName(ID INT PRIMARY KEY NOT NULL, created date NOT NULL)")
        }
        mem.createStatement().useAsync {
          it.executeUpdate("insert into $tableName(id,created) values(1,date('now'))")
        }
        mem.prepareStatement("insert into $tableName(id,created) values(?,?)").useAsync {
          it.set(0, 2)
          it.set(1, Date.now)
          it.executeUpdate()
        }
        mem.createStatement().useAsync {
          it.executeQuery("select created from $tableName").useAsync {
            while (it.next()) {
              println("->${it.getString(0)}")
              println("->${it.getLong(0)}")
              println("->${it.getDate(0)}")
            }
          }
        }
      }
    }
}
