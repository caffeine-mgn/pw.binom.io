package pw.binom.db.postgresql.async

import pw.binom.io.useAsync
import kotlin.test.Test
import kotlin.test.assertEquals

class TransactionTest : BaseTest() {
  @Test
  fun rollbackTest() {
    pg {
      it.executeUpdate(
        """
                create table if not exists tx_text
                (
                    id bigserial not null primary key,
                    date_column int8
                )
                """,
      )
      it.beginTransaction()
      it.executeUpdate(
        """
               insert into tx_text(date_column) values(10)
                """,
      )
      it.rollback()
    }

    pg {
      val bb =
        it.prepareStatement("""select * from tx_text""").useAsync {
          it.executeQuery().useAsync {
            if (it.next()) {
              it.getLong(0)
            } else {
              0L
            }
          }
        }
      assertEquals(0L, bb)
    }
  }
}
