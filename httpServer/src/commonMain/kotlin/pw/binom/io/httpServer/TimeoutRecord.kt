package pw.binom.io.httpServer

import kotlinx.coroutines.Job
import pw.binom.pool.ObjectFactory
import pw.binom.pool.ObjectPool

internal class TimeoutRecord {
  companion object : ObjectFactory<TimeoutRecord> {
    override fun allocate(pool: ObjectPool<TimeoutRecord>): TimeoutRecord = TimeoutRecord()

    override fun deallocate(value: TimeoutRecord, pool: ObjectPool<TimeoutRecord>) {
    }
  }

  var job: Job? = null
  var live: Long = 0
}
